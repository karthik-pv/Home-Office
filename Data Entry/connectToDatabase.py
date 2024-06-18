import json
from sqlalchemy import create_engine
from sqlalchemy import text


def read_json_file(file_path):
    try:
        with open(file_path, "r") as file:
            data = json.load(file)
            return data
    except FileNotFoundError:
        print(f"The file {file_path} does not exist.")
    except json.JSONDecodeError:
        print(f"Error decoding JSON from the file {file_path}.")


def connect_to_database():
    try:
        config = read_json_file("./config.json")
        if config is None:
            raise Exception("Failed to load configuration file.")

        db_details = config.get("DB_DETAILS", {})
        user = db_details.get("USER", "")
        password = db_details.get("PASSWORD", "")
        host = db_details.get("HOST", "")
        database = db_details.get("DATABASE", "")

        if not user or not password or not host or not database:
            raise Exception("Incomplete database configuration.")

        connection_string = f"postgresql://{user}:{password}@{host}/{database}"

        engine = create_engine(connection_string)
        connection = engine.connect()
        return connection
    except Exception as e:
        print(f"An exception has occured : {str(e)}")


def execute_query(engine, query):
    try:
        with engine as connection:
            result = connection.execute(query)
            return result.fetchall()
    except Exception as e:
        print(f"An exception has occured : {str(e)}")


def getListOfFundsDBLogic(fundTag, table):
    try:
        query = text(f'SELECT DISTINCT "{fundTag}" FROM "{table}"')
        return query
    except Exception as e:
        print(f"An exception has occured : {str(e)}")
