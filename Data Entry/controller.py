import os
from werkzeug.utils import secure_filename

from Database_Tier.dbLogic import getListOfFundsDBLogic
from Database_Tier.connectToDatabase import execute_query

from utils import ensure_upload_folder_exists

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


def uploadFileToServer(f):
    data_filename = secure_filename(f.filename)
    ensure_upload_folder_exists(UPLOAD_FOLDER)
    try:
        file_path = os.path.join(UPLOAD_FOLDER, data_filename)
        print(f"Saving file to: {file_path}")
        f.save(file_path)
    except PermissionError:
        return "Permission denied: unable to save file."
    except Exception as e:
        return f"An error occurred: {str(e)}"
