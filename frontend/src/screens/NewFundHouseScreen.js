import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import {
  DATA_ENTRY_BASE_URL,
  FILE_UPLOAD,
  GET_COLUMN_MAPPER_KEYS,
  GET_FUNDHOUSE_COLUMNS,
  GET_TRANSACTION_DESC_LIST,
  ADD_TRANSACTION_RELEVANCE_DATA,
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

  const handleSubmitFileUpload = async (event) => {
    event.preventDefault();
    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("table", fundHouseName);

      // Upload the file
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

      // Clear file input after uploading
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

  const handleSubmitColumnMapping = () => {
    alert("Column mapping submitted!");
    // You can handle the column mapping submission here
    console.log("Column Mapping:", columnMapping);
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
    <div className="flex flex-col min-h-screen bg-gray-900 p-6">
      <h1 className="text-white text-3xl font-bold mb-6 text-center">
        Add New Fundhouse
      </h1>

      {/* File Upload Form */}
      <form
        onSubmit={handleSubmitFileUpload}
        className="max-w-md mx-auto bg-gray-800 p-6 rounded-lg shadow-md"
      >
        <div className="mb-4">
          <label
            className="block text-white text-sm font-semibold mb-2"
            htmlFor="fundHouseName"
          >
            Fund House Name
          </label>
          <input
            type="text"
            id="fundHouseName"
            value={fundHouseName}
            onChange={(e) => setFundHouseName(e.target.value)}
            className="w-full px-4 py-2 bg-gray-700 text-white border border-gray-600 rounded focus:outline-none focus:border-green-500"
            required
          />
        </div>

        <div className="mb-4">
          <label
            className="block text-white text-sm font-semibold mb-2"
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
          className="w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 mt-6"
        >
          Submit File
        </button>
      </form>

      {/* Column Mapping Section */}
      <div className="mt-8">
        <h2 className="text-white text-2xl font-bold mb-4 text-center">
          Column Mapper
        </h2>
        {keys.length > 0 && values.length > 0 ? (
          <div className="flex flex-col items-center">
            {keys.map((key) => (
              <div
                key={key}
                className="mb-4 flex items-center justify-between w-full max-w-md"
              >
                <label className="text-white text-sm font-semibold">
                  {key}
                </label>
                <select
                  value={columnMapping[key] || ""}
                  onChange={(e) => handleMappingChange(key, e.target.value)}
                  className="px-4 py-2 bg-gray-700 text-white border border-gray-600 rounded w-1/2"
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
              onClick={handleSubmitColumnMapping}
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 mt-6"
            >
              Submit Column Mapping
            </button>
          </div>
        ) : (
          <p className="text-white text-center">Loading keys and values...</p>
        )}
      </div>

      {/* Transaction Relevance Section */}
      <div className="mt-8">
        <h2 className="text-white text-2xl font-bold mb-4 text-center">
          Transaction Relevance
        </h2>

        {values.length > 0 && (
          <div className="flex flex-col items-center mb-4">
            <label className="text-white text-sm font-semibold mb-2">
              Select Column for Transaction Description:
            </label>
            <select
              value={selectedColumn}
              onChange={(e) => setSelectedColumn(e.target.value)}
              className="px-4 py-2 bg-gray-700 text-white border border-gray-600 rounded w-1/2"
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
          <div className="flex flex-col items-center">
            {transactionDescriptions.map((desc) => (
              <div
                key={desc}
                className="mb-4 flex items-center justify-between w-full max-w-md"
              >
                <label className="text-white text-sm font-semibold">
                  {desc}
                </label>
                <div className="flex items-center">
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
                    className="mr-2"
                  />
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
              </div>
            ))}
            <button
              onClick={handleSubmitTransactionRelevance}
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 mt-6"
            >
              Submit Transaction Relevance
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
