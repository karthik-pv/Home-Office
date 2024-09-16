import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  DATA_ENTRY_BASE_URL,
  GET_FUNDHOUSES_LIST,
  GET_MASTER_TABLE,
  GET_SCHEMES_LIST,
} from "../urls/urls";

const XirrScreen = () => {
  const [fundHouseList, setFundHouseList] = useState([]);
  const [schemeList, setSchemeList] = useState([]);
  const [selectedFundHouse, setSelectedFundHouse] = useState(null);
  const [selectedScheme, setSelectedScheme] = useState(null);
  const [data, setData] = useState([]);

  const fetchData = async () => {
    try {
      const response = await axios.get(DATA_ENTRY_BASE_URL + GET_MASTER_TABLE, {
        params: {
          fundHouse: selectedFundHouse,
          fund_desc: selectedScheme,
        },
      });
      setData(JSON.parse(response.data));
    } catch (error) {
      console.error("Error fetching master table data:", error);
    }
  };

  useEffect(() => {
    fetchData();
  }, [selectedFundHouse, selectedScheme]);

  // Fetch fund house list
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

  // Fetch schemes based on selected fund house
  const fetchSchemes = async (fundHouse) => {
    try {
      const response = await axios.get(DATA_ENTRY_BASE_URL + GET_SCHEMES_LIST, {
        params: { fundHouse },
      });
      setSchemeList(response.data);
    } catch (error) {
      console.error("Error fetching schemes:", error);
    }
  };

  // Handle fund house selection
  const handleFundHouseChange = (event) => {
    const fundHouse = event.target.value;
    setSelectedFundHouse(fundHouse);
    setSelectedScheme(""); // Clear selected scheme when fund house changes
    if (fundHouse) {
      fetchSchemes(fundHouse);
    } else {
      setSchemeList([]);
    }
  };

  // Handle scheme selection
  const handleSchemeChange = (event) => {
    setSelectedScheme(event.target.value);
  };

  useEffect(() => {
    fetchFundHouseList();
  }, []);

  return (
    <div className="flex flex-col min-h-screen bg-gray-900 p-8">
      <h1 className="text-white text-3xl font-bold mb-8 text-center">
        Calculate XIRR
      </h1>

      <div className="max-w-lg mx-auto bg-gray-800 p-6 rounded-lg shadow-md">
        {/* Fund House Dropdown */}
        <div className="mb-4">
          <label
            className="block text-white text-sm font-semibold mb-2"
            htmlFor="fundHouseSelect"
          >
            Select Fund House
          </label>
          <select
            id="fundHouseSelect"
            value={selectedFundHouse}
            onChange={handleFundHouseChange}
            className="w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded text-sm"
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

        {/* Scheme Dropdown */}
        <div className="mb-4">
          <label
            className="block text-white text-sm font-semibold mb-2"
            htmlFor="schemeSelect"
          >
            Select Scheme
          </label>
          <select
            id="schemeSelect"
            value={selectedScheme}
            onChange={handleSchemeChange}
            className="w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded text-sm"
            disabled={!selectedFundHouse}
            required
          >
            <option value="">Select a Scheme</option>
            {schemeList.map((scheme) => (
              <option key={scheme} value={scheme}>
                {scheme}
              </option>
            ))}
          </select>
        </div>

        {/* Data Table */}
        <div className="overflow-x-auto overflow-y-auto max-h-screen">
          <table className="min-w-full bg-gray-800 text-white text-sm">
            <thead>
              <tr>
                {data.length > 0 &&
                  Object.keys(data[0]).map((key) => (
                    <th key={key} className="px-5 border-b border-gray-700">
                      {key}
                    </th>
                  ))}
              </tr>
            </thead>
            <tbody>
              {data.map((row, index) => (
                <tr key={index}>
                  {Object.values(row).map((value, idx) => (
                    <td key={idx} className="border-b border-gray-700">
                      {value}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Placeholder for XIRR Calculation */}
        {/* Add your XIRR calculation logic here */}
      </div>
    </div>
  );
};

export default XirrScreen;
