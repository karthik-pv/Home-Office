import os
from werkzeug.utils import secure_filename

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
)
from Database_Tier.schema import ColumnMapper, TransactionRelevance
from utils import ensure_upload_folder_exists, createDictionary

import pandas as pd

UPLOAD_FOLDER = os.path.join("Static_Files", "uploads")


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
