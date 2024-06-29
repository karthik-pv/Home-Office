import json
from sqlalchemy import create_engine, MetaData, Table, select, text
from sqlalchemy.orm import sessionmaker
from sqlalchemy import text
from Database_Tier.schema import ColumnMapper, MasterTable
from sqlalchemy.ext.declarative import declarative_base
from Database_Tier.schema import getBase


class DatabaseManager:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance.engine = cls._create_engine()
            cls._instance.Session = sessionmaker(bind=cls._instance.engine)
        return cls._instance

    @staticmethod
    def _create_engine():
        try:
            config = DatabaseManager.read_json_file("./config.json")
            db_details = config.get("DB_DETAILS", {})
            user = db_details.get("USER", "")
            password = db_details.get("PASSWORD", "")
            host = db_details.get("HOST", "")
            database = db_details.get("DATABASE", "")

            if not user or not password or not host or not database:
                raise Exception("Incomplete database configuration.")

            connection_string = f"postgresql://{user}:{password}@{host}/{database}"
            engine = create_engine(connection_string)
            return engine
        except Exception as e:
            print(f"An exception has occurred: {str(e)}")
            raise

    @staticmethod
    def read_json_file(file_path):
        try:
            with open(file_path, "r") as file:
                data = json.load(file)
                return data
        except FileNotFoundError:
            raise Exception(f"The file {file_path} does not exist.")
        except json.JSONDecodeError:
            raise Exception(f"Error decoding JSON from the file {file_path}.")

    @staticmethod
    def get_session():
        if DatabaseManager._instance is None:
            DatabaseManager._instance = DatabaseManager()
        return DatabaseManager._instance.Session()

    @staticmethod
    def get_engine():
        if DatabaseManager._instance is None:
            DatabaseManager._instance = DatabaseManager()
        return DatabaseManager._instance.engine


def execute_query(query):
    session = DatabaseManager.get_session()
    try:
        result = session.execute(query)
        session.commit()
        return result.fetchall()

    except Exception as e:
        session.rollback()
        print(f"An exception has occured : {str(e)}")

    finally:
        session.close()


def addToTable(data):
    session = DatabaseManager.get_session()
    try:
        session.add(data)
        session.commit()
    except Exception as e:
        session.rollback()
        print(f"An exception has occured : {str(e)}")
    finally:
        session.close()


def getEngine():
    return DatabaseManager.get_engine()


def reflection(table):
    engine = DatabaseManager.get_engine()
    session = DatabaseManager.get_session()
    metadata = MetaData()
    try:
        reflected_table = Table(table, metadata, autoload_with=engine)
        # query = reflected_table.select()
        # result = session.execute(query)
        # rows = result.fetchall()
        return reflected_table
    except Exception as e:
        print(f"An error occurred while reflecting the table: {str(e)}")
    finally:
        session.close()


# -------------------------------------------------------- LOGIC --------------------------------------------------------------------


def query_reflected_table(reflected_table):
    session = DatabaseManager.get_session()
    try:
        query = select(reflected_table)
        result = session.execute(query)
        return result
    except Exception as e:
        print(f"An error occurred while querying the table: {str(e)}")
        session.rollback()
    finally:
        session.close()


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


def fetchColumnMappings(fundName):
    session = DatabaseManager.get_session()
    try:
        query = select(ColumnMapper).where(ColumnMapper.FundHouse == fundName)
        result = session.execute(query).scalar_one()
        column_mappings = {
            column: getattr(result, column)
            for column in ColumnMapper.__table__.columns.keys()
            if getattr(result, column)
        }
        return column_mappings
    except Exception as e:
        print(f"An error occurred while fetching column mappings: {str(e)}")
        return None
    finally:
        session.close()


def transferDataToMasterTable(fundName, reflectedTable, columnMappings):
    engine = DatabaseManager.get_engine()
    session = DatabaseManager.get_session()
    Base = getBase()
    Base.metadata.create_all(engine)
    try:
        print(reflectedTable)
        print(columnMappings)
        query = select(reflectedTable)
        result = execute_query(query)
        for row in result:
            print(row)
            master_entry = MasterTable(
                FundHouse=fundName,
                InvestorName=row[columnMappings["InvestorName"]],
                TransactionDate=row[columnMappings["TransactionDate"]],
                TransactionDesc=row[columnMappings["TransactionDesc"]],
                Amount=row[columnMappings["Amount"]],
                NAV=row[columnMappings["NAV"]],
                Load=row[columnMappings["Load"]],
                Units=row[columnMappings["Units"]],
                FundDesc=row[columnMappings["FundDesc"]],
            )
            session.add(master_entry)

        session.commit()
        print("Data transferred successfully.")
    except Exception as e:
        session.rollback()
        print(f"An error occurred while transferring data: {str(e)}")
    finally:
        session.close()
