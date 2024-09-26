import React, { useState } from "react";
import axios from "axios";
import { ADD_SCHEME_MAPPER_URL, DATA_ENTRY_BASE_URL } from "../urls/urls";

const NAVSchemeMapperScreen = () => {
  const [schemeNameCSV, setSchemeNameCSV] = useState("");
  const [schemeNameNAVTable, setSchemeNameNAVTable] = useState("");

  const handleSubmit = async () => {
    if (!schemeNameCSV || !schemeNameNAVTable) {
      alert("Please fill out both scheme names");
      return;
    }

    const requestBody = {
      scheme_data: {
        [schemeNameCSV]: schemeNameNAVTable,
      },
    };

    try {
      await axios.post(
        DATA_ENTRY_BASE_URL + ADD_SCHEME_MAPPER_URL,
        requestBody
      );
      alert("Scheme Mapper Data added successfully!");
    } catch (error) {
      console.error("Error adding Scheme Mapper Data:", error);
      alert("Failed to add Scheme Mapper Data.");
    }
  };

  return (
    <div className="flex flex-col min-h-screen bg-gray-900 p-8">
      <h1 className="text-white text-3xl font-bold mb-8 text-center">
        Add Scheme Mapper Data
      </h1>

      <div className="max-w-lg mx-auto bg-gray-800 p-6 rounded-lg shadow-md">
        <div className="flex items-center mb-4 space-x-2">
          <input
            type="text"
            value={schemeNameCSV}
            onChange={(e) => setSchemeNameCSV(e.target.value)}
            className="flex-grow px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded text-sm"
            placeholder="Scheme Name 1"
          />
          <span className="text-white px-2">=</span>
          <input
            type="text"
            value={schemeNameNAVTable}
            onChange={(e) => setSchemeNameNAVTable(e.target.value)}
            className="flex-grow px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded text-sm"
            placeholder="Scheme Name 2"
          />
        </div>

        <button
          onClick={handleSubmit}
          className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-center"
        >
          Submit
        </button>
      </div>
    </div>
  );
};

export default NAVSchemeMapperScreen;
