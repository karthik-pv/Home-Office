from flask import Flask, request
from controller import (
    addToFundHouseList,
    getListofFundHouses,
    uploadFileToServer,
    addColumnMap,
    updateMasterTable,
    addTransactionRelevance,
    getColumnsOfFundHouse,
    getColumnMapperKeys,
    getListOfTransactionDesc,
)
import os
from flask import jsonify

UPLOAD_FOLDER = os.path.join("Static_Files", "uploads")

app = Flask(__name__)


@app.route("/getFundList")
def getListOfFunds():
    return jsonify(getListofFundHouses())


@app.route("/addFundToList", methods=["POST"])
def addToListOfFunds():
    data = request.json.get("fundHouseName")
    addToFundHouseList(data)
    return "Fund added to List"


@app.route("/fileUpload", methods=["POST"])
def uploadFile():
    f = request.files.get("file")
    table = request.form.get("table")
    if f:
        uploadFileToServer(f, table)
        return "file uploaded"
    else:
        return "please upload a file"


@app.route("/updateMasterTable", methods=["POST"])
def updateToMasterTable():
    data = request.json.get("fundName")
    updateMasterTable(data)
    return "Data updated in Master Table"


@app.route("/addColumnMapperData", methods=["POST"])
def addToColumnMapper():
    data = request.json.get("columnData")
    addColumnMap(data)
    return "Data added to Column Mapper"


@app.route("/addTransactionRelevanceData", methods=["POST"])
def addToTransactionRelevance():
    data = request.json.get("relevanceData")
    addTransactionRelevance(data)
    return "Data added to Transaction Relevance Table"


@app.route("/getFundHouseColumnList")
def getListOfTransactions():
    data = request.args.get("fundHouse")
    return jsonify(getColumnsOfFundHouse(data))


@app.route("/getColumnMapperKeys")
def getListOfKeys():
    return jsonify(getColumnMapperKeys())


@app.route("/getTransactionDescList")
def getTransactionsDescs():
    fundHouse = request.args.get("fundHouse")
    columnMappedToTrDesc = request.args.get("mappingColumn")
    return jsonify(getListOfTransactionDesc(fundHouse, columnMappedToTrDesc))


if __name__ == "__main__":
    app.run(debug=True)
