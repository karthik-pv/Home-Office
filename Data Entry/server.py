from flask import Flask, request
from controller import selectFundToInsertData, uploadFileToServer, addColumnMap
import os
from flask import jsonify

UPLOAD_FOLDER = os.path.join("Static_Files", "uploads")

app = Flask(__name__)


@app.route("/getFundList")
def getListOfFunds():
    return jsonify(selectFundToInsertData())


@app.route("/fileUpload", methods=["POST"])
def uploadFile():
    f = request.files.get("file")
    table = request.form.get("table")
    if f:
        uploadFileToServer(f, table)
    return "file uploaded"


@app.route("/addColumnMapperData", methods=["POST"])
def addToColumnMapper():
    data = request.json.get("columnData")
    addColumnMap(data)
    return "Data added to Column Mapper"


if __name__ == "__main__":
    app.run(debug=True)
