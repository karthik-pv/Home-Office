import json
from sqlalchemy import create_engine, MetaData, Table, select, text, inspect
from sqlalchemy.orm import sessionmaker
from sqlalchemy import text
from Database_Tier.schema import (
    ColumnMapper,
    MasterTable,
    TransactionRelevance,
    FundList,
)
from sqlalchemy.ext.declarative import declarative_base
from Database_Tier.schema import getBase


class DatabaseManager:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance.engine = cls._create_engine()
            base = getBase()
            base.metadata.create_all(cls._instance.engine)
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


def addToFundHouseList(fund_house_data):
    session = DatabaseManager.get_session()
    try:
        new_fund = FundList(fund_house=fund_house_data)
        session.add(new_fund)
        session.commit()
        print(f"Fund house '{fund_house_data}' added successfully.")
    except Exception as e:
        session.rollback()
        print(f"An error occurred while adding the fund house: {str(e)}")
    finally:
        session.close()


def getListOfFundsDBLogic():
    session = DatabaseManager.get_session()
    try:
        query = select(FundList.fund_house)
        result = session.execute(query).scalars().all()
        return result
    except Exception as e:
        session.rollback()
    finally:
        session.close()


def uploadCsvAsTable(dataframe, table):
    try:
        engine = getEngine()
        dataframe.to_sql(table, engine, if_exists="append", index=False)
    except Exception as e:
        print(f"An exception has occured : {str(e)}")


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


def fetchColumnMappings(fundName):
    session = DatabaseManager.get_session()
    try:
        query = select(ColumnMapper).where(ColumnMapper.fund_house == fundName)
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


def fetchTransactionRelevance(fundName):
    session = DatabaseManager.get_session()
    try:
        query = select(TransactionRelevance).where(
            TransactionRelevance.FundhouseName == fundName
        )
        results = session.execute(query).scalars().all()
        transaction_relevance = {
            getattr(result, "TransactionDesc"): getattr(result, "Offset")
            for result in results
            if getattr(result, "TransactionDesc") is not None
            and getattr(result, "Offset") is not None
        }
        return transaction_relevance
    except Exception as e:
        print(f"An error occurred while fetching column relevance: {str(e)}")
        return None
    finally:
        session.close()


def transferDataToMasterTable(
    fundName, reflectedTable, columnMappings, transactionRelevance
):
    engine = DatabaseManager.get_engine()
    session = DatabaseManager.get_session()
    Base = getBase()
    Base.metadata.create_all(engine)
    try:
        query = select(reflectedTable)
        result = execute_query(query)
        print(transactionRelevance)
        print(columnMappings)
        for row in result:
            if row[columnMappings["transaction_desc"]] in transactionRelevance:
                master_entry = MasterTable(
                    fund_house=fundName,
                    investor_name=row[columnMappings["investor_name"]],
                    transaction_date=row[columnMappings["transaction_date"]],
                    transaction_desc=row[columnMappings["transaction_desc"]],
                    amount=row[columnMappings["amount"]],
                    nav=row[columnMappings["nav"]],
                    load=row[columnMappings["load"]],
                    units=row[columnMappings["units"]],
                    bal_units=row[columnMappings["bal_units"]],
                    fund_desc=row[columnMappings["fund_desc"]],
                    net_transaction_amt=row[columnMappings["amount"]]
                    * transactionRelevance[row[columnMappings["transaction_desc"]]],
                    status=True,
                )
                print(master_entry)
                session.add(master_entry)

        session.commit()
        print("Data transferred successfully.")
    except Exception as e:
        session.rollback()
        print(f"An error occurred while transferring data: {str(e)}")
    finally:
        session.close()


def fetchColumnNames(table_name):
    engine = DatabaseManager.get_engine()
    inspector = inspect(engine)

    try:
        columns = inspector.get_columns(table_name)
        column_names = [column["name"] for column in columns]
        return column_names
    except Exception as e:
        print(f"An error occurred while fetching column names: {str(e)}")
        return None


def getTransactionDescsFromTable(fundName, columnMappingToTrDesc):
    engine = DatabaseManager.get_engine()
    session = DatabaseManager.get_session()

    try:

        metadata = MetaData()
        table = Table(fundName, metadata, autoload_with=engine)

        if columnMappingToTrDesc not in table.columns:
            raise ValueError(
                f"Column '{columnMappingToTrDesc}' does not exist in the table '{fundName}'."
            )

        query = select(table.c[columnMappingToTrDesc]).distinct()

        result = session.execute(query)
        unique_transaction_descs = [row[0] for row in result.fetchall()]

        return unique_transaction_descs

    except Exception as e:
        print(f"An error occurred while fetching transaction descriptions: {str(e)}")
        return None

    finally:
        session.close()
