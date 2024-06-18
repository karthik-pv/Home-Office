from flask import Flask
from controller import getListofFunds
from connectToDatabase import connect_to_database

app = Flask(__name__)

db = connect_to_database()


@app.route("/")
def getListOfFunds():
    return getListofFunds(db)


if __name__ == "__main__":
    app.run(debug=True)
