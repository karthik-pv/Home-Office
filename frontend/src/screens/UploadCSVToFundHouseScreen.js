import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import {
  DATA_ENTRY_BASE_URL,
  FILE_UPLOAD,
  GET_FUNDHOUSES_LIST,
  UPDATE_MASTER_TABLE,
} from "../urls/urls";

const UploadCsvToFundHouseScreen = () => {
  const [fundHouseList, setFundHouseList] = useState([]);
  const [selectedFundHouse, setSelectedFundHouse] = useState("");
  const [file, setFile] = useState(null);
  const fileInputRef = useRef(null);

  // Fetch the list of fund houses
  const fetchFundHouseList = async () => {
    try {
      const response = await axios.get(
        DATA_ENTRY_BASE_URL + GET_FUNDHOUSES_LIST
      );
      setFundHouseList(response.data);
    } catch (error) {
      console.error("Error fetching fund house list:", error);
    }
  };

  // Handle CSV file selection
  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  // Handle file upload only
  const handleFileUpload = async (event) => {
    event.preventDefault();

    if (!selectedFundHouse) {
      alert("Please select a fund house.");
      return;
    }

    if (!file) {
      alert("Please select a file to upload.");
      return;
    }

    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("table", selectedFundHouse);

      // Upload file
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

      // Clear file input after upload
      setFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }

      alert("File uploaded successfully!");
    } catch (error) {
      console.error("Error uploading file:", error);
      alert("An error occurred while uploading the file.");
    }
  };

  // Handle master table update only
  const handleMasterTableUpdate = async () => {
    if (!selectedFundHouse) {
      alert("Please select a fund house.");
      return;
    }

    try {
      const response = await axios.post(
        DATA_ENTRY_BASE_URL + UPDATE_MASTER_TABLE,
        {
          fundName: selectedFundHouse,
        }
      );
      console.log("Master table updated:", response.data);
      alert("Master table updated successfully!");
    } catch (error) {
      console.log("Error updating master table", error.message);
      alert("An error occurred while updating the master table.");
    }
  };

  useEffect(() => {
    fetchFundHouseList();
  }, []);

  return (
    <div className="flex flex-col min-h-screen bg-gray-900 p-8">
      <h1 className="text-white text-3xl font-bold mb-8 text-center">
        Upload CSV to Fundhouse
      </h1>

      {/* File Upload Form */}
      <form className="max-w-lg mx-auto bg-gray-800 p-8 rounded-lg shadow-md">
        {/* Fund House Dropdown */}
        <div className="mb-6">
          <label
            className="block text-white text-sm font-semibold mb-3"
            htmlFor="fundHouseSelect"
          >
            Select Fund House
          </label>
          <select
            id="fundHouseSelect"
            value={selectedFundHouse}
            onChange={(e) => setSelectedFundHouse(e.target.value)}
            className="w-full px-4 py-3 bg-gray-700 text-white border border-gray-600 rounded"
            required
          >
            <option value="">Select a Fund House</option>
            {fundHouseList.map((fundHouse) => (
              <option key={fundHouse} value={fundHouse}>
                {fundHouse}
              </option>
            ))}
          </select>
        </div>

        {/* File Upload */}
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
            ref={fileInputRef}
          />
        </div>

        {/* Separate Buttons for File Upload and Master Table Update */}
        <div className="flex space-x-4">
          <button
            type="button"
            onClick={handleFileUpload}
            className="w-full px-4 py-3 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Upload File
          </button>
          <button
            type="button"
            onClick={handleMasterTableUpdate}
            className="w-full px-4 py-3 bg-green-600 text-white rounded hover:bg-green-700"
          >
            Update Master Table
          </button>
        </div>
      </form>
    </div>
  );
};

export default UploadCsvToFundHouseScreen;
