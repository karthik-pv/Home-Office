from flask import Flask, request
from flask_cors import CORS
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
    getFundSchemes,
    get_master_table_as_json,
    truncateMasterTable,
    fetch_and_store_nav_data,
    addSchemeMap,
    getFromSchemeMap,
    getNAVValueFromTable,
)
import os
from flask import jsonify
from grpcServer import serveGrpc
from threading import Thread
import sys
import signal


UPLOAD_FOLDER = os.path.join("Static_Files", "uploads")

app = Flask(__name__)

cors = CORS(app)


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
    clearMasterTable()
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
    print(data)
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


@app.route("/getSchemesOfFundHouse")
def getSchemesOfFundHouse():
    fundHouse = request.args.get("fundHouse")
    return jsonify(getFundSchemes(fundHouse))


@app.route("/getMasterTable")
def getMasterTable():
    fund_house = request.args.get("fundHouse")
    fund_desc = request.args.get("fund_desc")
    return jsonify(get_master_table_as_json(fund_house, fund_desc))


@app.route("/clearMasterTable")
def clearMasterTable():
    truncateMasterTable()
    return jsonify("master table cleared")


@app.route("/getAndStoreNAVData")
def getNAVDataFromWeb():
    return jsonify(fetch_and_store_nav_data())


@app.route("/addSchemeMapperData", methods=["POST"])
def updateSchemeMapper():
    data = request.json.get("scheme_data")
    addSchemeMap(data)
    return "Data added to scheme mapper"


@app.route("/getSchemeMapping")
def getSchemeMap():
    scheme = request.args.get("scheme")
    return jsonify(getFromSchemeMap(scheme))


@app.route("/getNAVForScheme")
def getNAV():
    scheme = request.args.get("scheme")
    return jsonify(getNAVValueFromTable(scheme))


def serveHttp():
    fetch_and_store_nav_data()
    app.run(debug=False, use_reloader=False)


def signal_handler(sig, frame):
    print("Shutting down servers...")
    sys.exit(0)


if __name__ == "__main__":
    signal.signal(signal.SIGINT, signal_handler)

    grpc_thread = Thread(target=serveGrpc)
    http_thread = Thread(target=serveHttp)

    grpc_thread.start()
    http_thread.start()

    try:
        grpc_thread.join()
        http_thread.join()
    except KeyboardInterrupt:
        print("Interrupted! Shutting down...")
