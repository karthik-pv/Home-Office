from sqlalchemy import text
from Database_Tier.connectToDatabase import getEngine


def getListOfFundsDBLogic(fundTag, table):
    try:
        query = text(f'SELECT DISTINCT "{fundTag}" FROM "{table}"')
        return query
    except Exception as e:
        print(f"An exception has occured : {str(e)}")


def uploadCsvAsTable(dataframe, table):
    try:
        engine = getEngine()
        dataframe.to_sql(table, engine, if_exists="append", index=False)
    except Exception as e:
        print(f"An exception has occured : {str(e)}")
