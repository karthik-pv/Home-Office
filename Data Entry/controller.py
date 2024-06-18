from connectToDatabase import getListOfFundsDBLogic, execute_query


def getListofFunds(db):
    query = getListOfFundsDBLogic("Fund", "NAV")
    funds = execute_query(db, query)
    print(funds)
