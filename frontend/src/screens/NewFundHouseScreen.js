import React, { useState } from "react";
import axios from "axios";

const NewFundHouseScreen = () => {
  const [fundHouseName, setFundHouseName] = useState("");
  const [file, setFile] = useState(null);
  const [transactionRelevance, setTransactionRelevance] = useState([]);
  const [selectedRelevance, setSelectedRelevance] = useState({});

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    // Upload CSV and fetch column mapper and transaction relevance data
    const formData = new FormData();
    formData.append("file", file);
    formData.append("fundHouseName", fundHouseName);

    try {
      // Replace the URL with your backend endpoint
      const response = await axios.post("/api/upload-csv", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      const { columnMapper, transactionRelevance } = response.data;

      setTransactionRelevance(transactionRelevance);
      const initialRelevance = {};
      transactionRelevance.forEach((item) => {
        initialRelevance[item.id] = 0;
      });
      setSelectedRelevance(initialRelevance);
    } catch (error) {
      console.error("Error uploading file:", error);
    }
  };

  const handleRelevanceChange = (id, value) => {
    setSelectedRelevance((prev) => ({
      ...prev,
      [id]: value,
    }));
  };

  return (
    <div className="flex flex-col min-h-screen bg-gray-900 p-6">
      <h1 className="text-white text-3xl font-bold mb-6 text-center">
        Add New Fundhouse
      </h1>
      <form
        onSubmit={handleSubmit}
        className="max-w-md mx-auto bg-gray-800 p-6 rounded-lg shadow-md"
      >
        {/* Fund House Name */}
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

        {/* File Upload */}
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
          />
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          className="w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
        >
          Submit
        </button>

        {/* Transaction Relevance */}
        <div className="mt-6">
          <h2 className="text-white text-xl font-semibold mb-4">
            Select Transaction Relevance
          </h2>
          {transactionRelevance.map((item) => (
            <div key={item.id} className="mb-4">
              <div className="flex items-center mb-2">
                <input
                  type="checkbox"
                  id={`relevance-${item.id}`}
                  checked={selectedRelevance.hasOwnProperty(item.id)}
                  onChange={(e) =>
                    handleRelevanceChange(
                      item.id,
                      e.target.checked ? selectedRelevance[item.id] : 0
                    )
                  }
                  className="mr-2"
                />
                <label className="text-white">{item.description}</label>
              </div>
              {selectedRelevance.hasOwnProperty(item.id) && (
                <select
                  value={selectedRelevance[item.id]}
                  onChange={(e) =>
                    handleRelevanceChange(item.id, parseInt(e.target.value))
                  }
                  className="w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded"
                >
                  <option value={-1}>-1</option>
                  <option value={0}>0</option>
                  <option value={1}>1</option>
                </select>
              )}
            </div>
          ))}
        </div>
      </form>
    </div>
  );
};

export default NewFundHouseScreen;
