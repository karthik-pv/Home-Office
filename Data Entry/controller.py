import os
from werkzeug.utils import secure_filename

from Database_Tier.dbLogic import getListOfFundsDBLogic, uploadCsvAsTable
from Database_Tier.connectToDatabase import execute_query, addToTable
from Database_Tier.schema import ColumnMapper

from utils import ensure_upload_folder_exists

import pandas as pd

UPLOAD_FOLDER = os.path.join("Static_Files", "uploads")


def getListofFunds():
    query = getListOfFundsDBLogic("Fund", "NAV")
    funds = execute_query(query)
    result_list = [item[0] for item in funds]
    return result_list


def selectFundToInsertData():
    fund_list = getListofFunds()
    fund_list.sort()
    return fund_list


def uploadFileToServer(f, tableName):
    data_filename = secure_filename(f.filename)
    ensure_upload_folder_exists(UPLOAD_FOLDER)
    try:
        file_path = os.path.join(UPLOAD_FOLDER, data_filename)
        print(f"Saving file to: {file_path}")
        f.save(file_path)
        addDataToFundTable(tableName, file_path)
    except PermissionError:
        return "Permission denied: unable to save file."
    except Exception as e:
        return f"An error occurred: {str(e)}"


def addDataToFundTable(table, file):
    try:
        df = pd.read_csv(file)
        df_no_null = df.dropna(how="all")
        uploadCsvAsTable(df_no_null, table)
    except Exception as e:
        return f"An error occurred: {str(e)}"


def addColumnMap(data):
    try:
        newEntry = ColumnMapper(**data)
        addToTable(newEntry)
        return
    except Exception as e:
        return f"An error occurred: {str(e)}"
