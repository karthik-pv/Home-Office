from Database_Tier.dbLogic import getListOfFundsDBLogic
from Database_Tier.connectToDatabase import execute_query


def getListofFunds():
    query = getListOfFundsDBLogic("Fund", "NAV")
    funds = execute_query(query)
    result_list = [item[0] for item in funds]
    return result_list
