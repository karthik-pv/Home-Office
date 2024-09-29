import os
from werkzeug.utils import secure_filename
import requests

from Database_Tier.connectToDatabase import (
    addToFundHouseList,
    addToTable,
    getListOfFundsDBLogic,
    uploadCsvAsTable,
    fetchColumnMappings,
    reflection,
    transferDataToMasterTable,
    fetchTransactionRelevance,
    fetchColumnNames,
    getTransactionDescsFromTable,
    getUniqueFundDescFromMasterTable,
    get_masterTable_as_json,
    truncate_master_table,
    fetchMappedScheme,
    getNAVFromTable,
)
from Database_Tier.schema import (
    ColumnMapper,
    TransactionRelevance,
    schemeNameNAVTableMapper,
)
from utils import ensure_upload_folder_exists, createDictionary

import pandas as pd

UPLOAD_FOLDER = os.path.join("Static_Files", "uploads")
NAV_URL = "https://www.amfiindia.com/spages/NAVAll.txt"


def addNewFundHouse(data):
    addToFundHouseList(data)
    return


def getListofFundHouses():
    fund_list = getListOfFundsDBLogic()
    print(fund_list)
    fund_list.sort()
    return fund_list


def addTransactionDataToIndividualFundTable(table, file):
    try:
        df = pd.read_csv(file)
        df_no_null = df.dropna(how="all")
        uploadCsvAsTable(df_no_null, table)
    except Exception as e:
        return f"An error occurred: {str(e)}"


def uploadFileToServer(f, tableName):
    data_filename = secure_filename(f.filename)
    ensure_upload_folder_exists(UPLOAD_FOLDER)
    try:
        file_path = os.path.join(UPLOAD_FOLDER, data_filename)
        print(f"Saving file to: {file_path}")
        f.save(file_path)
        addTransactionDataToIndividualFundTable(tableName, file_path)
    except PermissionError:
        return "Permission denied: unable to save file."
    except Exception as e:
        return f"An error occurred: {str(e)}"


def getColumnsOfFundHouse(fundHouseName):
    columns = fetchColumnNames(fundHouseName)
    print(columns)
    return columns


def getColumnMapperKeys():
    return ColumnMapper.__mapper__.columns.keys()


def addColumnMap(data):
    try:
        newEntry = ColumnMapper(**data)
        addToTable(newEntry)
        return
    except Exception as e:
        return f"An error occurred: {str(e)}"


def addTransactionRelevance(data):
    try:
        newEntry = TransactionRelevance(**data)
        addToTable(newEntry)
        return
    except Exception as e:
        return f"An error occured: {str(e)}"


def updateMasterTable(fundName):
    try:
        columnMap = fetchColumnMappings(fundName)
        transactionRelevance = fetchTransactionRelevance(fundName)
        reflectedTable = reflection(fundName)
        createDictionary(reflectedTable, columnMap)
        print(reflectedTable)
        transferDataToMasterTable(
            fundName=fundName,
            reflectedTable=reflectedTable,
            columnMappings=columnMap,
            transactionRelevance=transactionRelevance,
        )
    except Exception as e:
        return f"An error occurred: {str(e)}"


def getListOfTransactionDesc(fundName, columnMappingToTrDesc):
    return getTransactionDescsFromTable(fundName, columnMappingToTrDesc)


def getFundSchemes(fundName):
    return getUniqueFundDescFromMasterTable(fundName)


def get_master_table_as_json(fund_house, fund_desc):
    return get_masterTable_as_json(fund_house, fund_desc)


def truncateMasterTable():
    truncate_master_table()


def fetch_and_store_nav_data():
    try:
        response = requests.get(NAV_URL)
        response.raise_for_status()

        raw_data = response.text.splitlines()
        data = []
        for line in raw_data:
            if ";" in line:
                data.append(line.split(";"))

        df = pd.DataFrame(
            data,
            columns=[
                "Scheme Code",
                "ISIN Div Payout/ ISIN Growth",
                "ISIN Div Reinvestment",
                "Scheme Name",
                "Net Asset Value",
                "Date",
            ],
        )
        df["Net Asset Value"] = pd.to_numeric(df["Net Asset Value"], errors="coerce")
        df["Date"] = pd.to_datetime(df["Date"], format="%Y-%m-%d", errors="coerce")

        uploadCsvAsTable(df, "nav_holder")

        return {"message": "Data successfully inserted into PostgreSQL"}
    except Exception as e:
        return {"error": str(e)}, 500


def addSchemeMap(data):
    try:
        for scheme_name_balance_sheet, scheme_name_nav_table in data.items():
            newEntry = schemeNameNAVTableMapper(
                schemeNameBalanceSheet=scheme_name_balance_sheet,
                schemeNameNAVTable=scheme_name_nav_table,
            )
            existing_entry = fetchMappedScheme(scheme_name_balance_sheet)
            if existing_entry is not None:
                print(
                    f"Entry for '{scheme_name_balance_sheet}' already exists. Skipping."
                )
                continue
            addToTable(newEntry)

        return "Data added successfully to scheme mapper."
    except Exception as e:
        return f"An error occurred: {str(e)}"


def getFromSchemeMap(data):
    try:
        result = fetchMappedScheme(data)
        if result is not None:
            return result
        else:
            return "No mapped scheme found"
    except Exception as e:
        print(f"An error occurred while getting from scheme map: {str(e)}")


def getNAVValueFromTable(scheme):
    try:
        mappedScheme = fetchMappedScheme(scheme)
        print(mappedScheme)
        return getNAVFromTable(mappedScheme)
    except Exception as e:
        print(f"An error occurred while getting NAV value: {str(e)}")
        return None
