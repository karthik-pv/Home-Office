import json
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker


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
