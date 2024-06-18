from flask import Flask
from controller import getListofFunds

from flask import jsonify


app = Flask(__name__)


@app.route("/")
def getListOfFunds():
    return jsonify(getListofFunds())


if __name__ == "__main__":
    app.run(debug=True)
