import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import {
  DATA_ENTRY_BASE_URL,
  FILE_UPLOAD,
  GET_COLUMN_MAPPER_KEYS,
  GET_FUNDHOUSE_COLUMNS,
  GET_TRANSACTION_DESC_LIST,
  ADD_TRANSACTION_RELEVANCE_DATA,
  ADD_COLUMN_MAPPER_DATA,
  ADD_FUND_TO_LIST,
  UPDATE_MASTER_TABLE,
} from "../urls/urls";

const NewFundHouseScreen = () => {
  const [fundHouseName, setFundHouseName] = useState("");
  const [file, setFile] = useState(null);
  const [keys, setKeys] = useState([]);
  const [values, setValues] = useState([]);
  const [selectedColumn, setSelectedColumn] = useState("");
  const [columnMapping, setColumnMapping] = useState({});
  const [transactionDescriptions, setTransactionDescriptions] = useState([]);
  const [transactionRelevance, setTransactionRelevance] = useState({});
  const fileInputRef = useRef(null);

  const fetchKeys = async () => {
    try {
      const response = await axios.get(
        DATA_ENTRY_BASE_URL + GET_COLUMN_MAPPER_KEYS
      );
      setKeys(response.data);
    } catch (error) {
      console.error("Error fetching keys:", error);
    }
  };

  const fetchValues = async () => {
    try {
      const response = await axios.get(
        DATA_ENTRY_BASE_URL + GET_FUNDHOUSE_COLUMNS,
        {
          params: { fundHouse: fundHouseName },
        }
      );
      setValues(response.data);
    } catch (error) {
      console.error("Error fetching fundHouse columns:", error);
    }
  };

  const fetchTransactionDescriptions = async () => {
    if (!selectedColumn) return;

    try {
      const response = await axios.get(
        DATA_ENTRY_BASE_URL + GET_TRANSACTION_DESC_LIST,
        {
          params: { fundHouse: fundHouseName, mappingColumn: selectedColumn },
        }
      );
      setTransactionDescriptions(response.data);
    } catch (error) {
      console.error("Error fetching transaction descriptions:", error);
    }
  };

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  const handleMappingChange = (key, value) => {
    setColumnMapping((prevMapping) => ({
      ...prevMapping,
      [key]: value,
    }));
  };

  const updateMasterTable = async () => {
    try {
      const response = await axios.post(
        DATA_ENTRY_BASE_URL + UPDATE_MASTER_TABLE,
        {
          fundName: fundHouseName,
        }
      );
      console.log("Column mapper updated:", response.data);
    } catch (error) {
      console.log("Error updating master table" + error.message);
    }
  };

  const handleSubmitFileUpload = async (event) => {
    event.preventDefault();
    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("table", fundHouseName);

      const updateList = await axios.post(
        DATA_ENTRY_BASE_URL + ADD_FUND_TO_LIST,
        {
          fundHouseName,
        }
      );
      const fileResponse = await axios.post(
        DATA_ENTRY_BASE_URL + FILE_UPLOAD,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );
      console.log("File uploaded:", fileResponse.data);

      setFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }

      fetchKeys();
      fetchValues();
      alert(
        "File uploaded successfully! Now please select a column to fetch transaction descriptions."
      );
    } catch (error) {
      console.error("Error submitting file:", error);
      alert("An error occurred while uploading the file.");
    }
  };

  const handleColumnMappingSubmit = async () => {
    const columnData = {
      ...columnMapping,
      fund_house: fundHouseName,
    };

    try {
      const response = await axios.post(
        DATA_ENTRY_BASE_URL + ADD_COLUMN_MAPPER_DATA,
        { columnData }
      );
      console.log(response);
    } catch (error) {
      console.error("Error saving column mapping:", error);
    }
  };

  const handleSubmitTransactionRelevance = async () => {
    try {
      for (const [desc, { isSelected, offset }] of Object.entries(
        transactionRelevance
      )) {
        if (isSelected) {
          await axios.post(
            DATA_ENTRY_BASE_URL + ADD_TRANSACTION_RELEVANCE_DATA,
            {
              relevanceData: {
                FundhouseName: fundHouseName,
                TransactionDesc: desc,
                Offset: offset,
              },
            }
          );
          console.log(`Submitted relevance data for: ${desc}`);
        }
      }
      alert("Transaction relevance data submitted!");
    } catch (error) {
      console.error("Error submitting transaction relevance data:", error);
      alert("An error occurred while submitting transaction relevance data.");
    }
  };

  const handleTransactionRelevanceChange = (desc, field, value) => {
    setTransactionRelevance((prev) => ({
      ...prev,
      [desc]: {
        ...prev[desc],
        [field]: value,
      },
    }));
  };

  useEffect(() => {
    if (selectedColumn) {
      fetchTransactionDescriptions();
    }
  }, [selectedColumn]);

  return (
    <div className="flex flex-col min-h-screen bg-gray-900 p-8">
      <h1 className="text-white text-3xl font-bold mb-8 text-center">
        Add New Fundhouse
      </h1>

      {/* File Upload Form */}
      <form
        onSubmit={handleSubmitFileUpload}
        className="max-w-lg mx-auto bg-gray-800 p-8 rounded-lg shadow-md"
      >
        <div className="mb-6">
          <label
            className="block text-white text-sm font-semibold mb-3"
            htmlFor="fundHouseName"
          >
            Fund House Name
          </label>
          <input
            type="text"
            id="fundHouseName"
            value={fundHouseName}
            onChange={(e) => setFundHouseName(e.target.value)}
            className="w-full px-4 py-3 bg-gray-700 text-white border border-gray-600 rounded focus:outline-none focus:border-green-500"
            required
          />
        </div>

        <div className="mb-6">
          <label
            className="block text-white text-sm font-semibold mb-3"
            htmlFor="fileUpload"
          >
            Upload CSV
          </label>
          <input
            type="file"
            id="fileUpload"
            onChange={handleFileChange}
            className="w-full text-white bg-gray-700 border border-gray-600 rounded"
            accept=".csv"
            required
            ref={fileInputRef}
          />
        </div>

        <button
          type="submit"
          className="w-full px-4 py-3 bg-green-600 text-white rounded hover:bg-green-700"
        >
          Submit File
        </button>
      </form>

      {/* Column Mapping Section */}
      <div className="mt-12">
        <h2 className="text-white text-2xl font-bold mb-6 text-center">
          Column Mapper
        </h2>
        {keys.length > 0 && values.length > 0 ? (
          <div className="flex flex-col items-center">
            {keys.map((key) => (
              <div
                key={key}
                className="mb-6 flex items-center justify-between w-full max-w-lg"
              >
                <label className="text-white text-sm font-semibold">
                  {key}
                </label>
                <select
                  value={columnMapping[key] || ""}
                  onChange={(e) => handleMappingChange(key, e.target.value)}
                  className="px-4 py-3 bg-gray-700 text-white border border-gray-600 rounded w-1/2"
                >
                  <option value="">Select Value</option>
                  {values.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </select>
              </div>
            ))}
            <button
              onClick={handleColumnMappingSubmit}
              className="px-4 py-3 bg-blue-600 text-white rounded hover:bg-blue-700 mt-8"
            >
              Submit Column Mapping
            </button>
          </div>
        ) : (
          <p className="text-white text-center">Loading keys and values...</p>
        )}
      </div>

      {/* Transaction Relevance Section */}
      <div className="mt-12">
        <h2 className="text-white text-2xl font-bold mb-6 text-center">
          Transaction Relevance
        </h2>

        {values.length > 0 && (
          <div className="flex flex-col items-center mb-6">
            <label className="text-white text-sm font-semibold mb-3">
              Select Column for Transaction Description:
            </label>
            <select
              value={selectedColumn}
              onChange={(e) => setSelectedColumn(e.target.value)}
              className="px-4 py-3 bg-gray-700 text-white border border-gray-600 rounded w-1/2"
            >
              <option value="">Select Column</option>
              {values.map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
          </div>
        )}

        {transactionDescriptions.length > 0 ? (
          <div className="flex flex-col items-center text-center justify-center">
            {transactionDescriptions.map((desc) => (
              <div
                key={desc}
                className="mb-6 flex flex-col w-3/4 max-w-lg bg-gray-800 p-4 rounded-lg shadow-md justify-center items-center"
              >
                <div className="mb-4">
                  <span className="text-white font-semibold text-xl">
                    {desc}
                  </span>
                </div>
                <div className="flex items-center space-x-4 mb-4">
                  <label className="text-white text-sm font-semibold">
                    Offset
                  </label>
                  <select
                    value={transactionRelevance[desc]?.offset || ""}
                    onChange={(e) =>
                      handleTransactionRelevanceChange(
                        desc,
                        "offset",
                        parseInt(e.target.value, 10)
                      )
                    }
                    className="px-4 py-2 bg-gray-700 text-white border border-gray-600 rounded"
                  >
                    <option value="">Select Offset</option>
                    <option value="1">1</option>
                    <option value="-1">-1</option>
                  </select>
                </div>
                <div className="flex items-center space-x-4">
                  <input
                    type="checkbox"
                    checked={transactionRelevance[desc]?.isSelected || false}
                    onChange={(e) =>
                      handleTransactionRelevanceChange(
                        desc,
                        "isSelected",
                        e.target.checked
                      )
                    }
                    className="h-5 w-5 text-green-600 border-gray-600 rounded"
                  />
                  <span className="text-white text-sm font-semibold">
                    Include
                  </span>
                </div>
              </div>
            ))}

            <button
              onClick={handleSubmitTransactionRelevance}
              className="px-4 py-3 bg-blue-600 text-white rounded hover:bg-blue-700 mt-8"
            >
              Submit Transaction Relevance
            </button>
            <button
              onClick={updateMasterTable}
              className="px-4 py-3 bg-red-600 text-white rounded hover:bg-red-700 mt-8 w-1/4 items-center justify-center"
            >
              Update Master Table
            </button>
          </div>
        ) : (
          <p className="text-white text-center">
            No transaction descriptions available.
          </p>
        )}
      </div>
    </div>
  );
};

export default NewFundHouseScreen;
