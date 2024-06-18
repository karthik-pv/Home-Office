from sqlalchemy import text


def getListOfFundsDBLogic(fundTag, table):
    try:
        query = text(f'SELECT DISTINCT "{fundTag}" FROM "{table}"')
        return query
    except Exception as e:
        print(f"An exception has occured : {str(e)}")
