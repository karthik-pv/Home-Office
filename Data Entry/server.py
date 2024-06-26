from flask import Flask, request
from controller import getListofFunds, selectFundToInsertData, uploadFileToServer
import os
from flask import jsonify
from werkzeug.utils import secure_filename

UPLOAD_FOLDER = os.path.join("Static_Files", "uploads")

app = Flask(__name__)


@app.route("/")
def getListOfFunds():
    return jsonify(selectFundToInsertData())


@app.route("/fileUpload", methods=["POST"])
def uploadFile():
    f = request.files.get("file")
    table = request.form.get("table")
    if f:
        uploadFileToServer(f, table)
    return "file uploaded"


if __name__ == "__main__":
    app.run(debug=True)
