import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  BUSINESS_LOGIC_BASE_URL,
  DATA_ENTRY_BASE_URL,
  GET_ABS_RETURN_TOTAL,
  GET_ABS_RETURN_BALANCE,
  GET_ABS_RETURN_CUSTOM,
  GET_ABS_RETURN_SOLD,
  GET_FUNDHOUSES_LIST,
  GET_MASTER_TABLE,
  GET_SCHEMES_LIST,
} from "../urls/urls";

const AbsoluteReturnScreen = () => {
  const [fundHouseList, setFundHouseList] = useState([]);
  const [schemeList, setSchemeList] = useState([]);
  const [selectedFundHouse, setSelectedFundHouse] = useState(null);
  const [selectedScheme, setSelectedScheme] = useState(null);
  const [data, setData] = useState([]);
  const [units, setUnits] = useState(0);

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
    if (selectedFundHouse && selectedScheme) {
      fetchData();
    }
  }, [selectedFundHouse, selectedScheme]);

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

  const fetchSchemes = async (fundHouse) => {
    try {
      const response = await axios.get(DATA_ENTRY_BASE_URL + GET_SCHEMES_LIST, {
        params: { fundHouse },
      });
      setSchemeList(response.data);
      if (response.data.length === 0) {
        alert("Update Master Table First");
      }
    } catch (error) {
      console.error("Error fetching schemes:", error);
    }
  };

  const handleFundHouseChange = (event) => {
    const fundHouse = event.target.value;
    setSelectedFundHouse(fundHouse);
    setSelectedScheme("");
    if (fundHouse) {
      fetchSchemes(fundHouse);
    } else {
      setSchemeList([]);
    }
  };

  const handleSchemeChange = (event) => {
    setSelectedScheme(event.target.value);
  };

  const handleUnitsChange = (event) => {
    setUnits(event.target.value);
  };

  useEffect(() => {
    fetchFundHouseList();
  }, []);

  const handleTotalAbsoluteReturn = async () => {
    if (!selectedFundHouse) {
      alert("Please select Fund House");
      return;
    }

    let requestBody = {
      fundhouse: selectedFundHouse,
      schemes: schemeList,
    };

    if (selectedScheme) {
      requestBody.schemes = [selectedScheme];
    }

    try {
      const response = await axios.post(
        BUSINESS_LOGIC_BASE_URL + GET_ABS_RETURN_TOTAL,
        requestBody
      );
      const absReturnValue = response.data;
      alert(`Total Absolute Return: ${absReturnValue}`);
    } catch (error) {
      console.error("Error fetching Total Absolute Return:", error);
      alert("Error fetching Total Absolute Return.");
    }
  };

  const handleBalanceUnitsAbsoluteReturn = async () => {
    if (!selectedFundHouse) {
      alert("Please select Fund House");
      return;
    }

    let requestBody = {
      fundhouse: selectedFundHouse,
      schemes: schemeList,
    };

    if (selectedScheme) {
      requestBody.schemes = [selectedScheme];
    }

    try {
      const response = await axios.post(
        BUSINESS_LOGIC_BASE_URL + GET_ABS_RETURN_BALANCE,
        requestBody
      );
      const absReturnValue = response.data;
      alert(`Absolute Return for Balance Units: ${absReturnValue}`);
    } catch (error) {
      console.error("Error fetching Absolute Return for Balance Units:", error);
      alert("Error fetching Absolute Return for Balance Units.");
    }
  };

  const handleCustomUnitsAbsoluteReturn = async () => {
    if (!selectedFundHouse) {
      alert("Please select Fund House");
      return;
    }

    let requestBody = {
      fundhouse: selectedFundHouse,
      schemes: schemeList,
      units: units,
    };

    if (selectedScheme) {
      requestBody.schemes = [selectedScheme];
    }

    try {
      const response = await axios.post(
        BUSINESS_LOGIC_BASE_URL + GET_ABS_RETURN_CUSTOM,
        requestBody
      );
      const absReturnValue = response.data;
      alert(`Absolute Return for Custom Units: ${absReturnValue}`);
    } catch (error) {
      console.error("Error fetching Absolute Return for Custom Units:", error);
      alert("Error fetching Absolute Return for Custom Units.");
    }
  };

  const handleSoldUnitsAbsoluteReturn = async () => {
    if (!selectedFundHouse) {
      alert("Please select Fund House");
      return;
    }

    let requestBody = {
      fundhouse: selectedFundHouse,
      schemes: schemeList,
      units: units,
    };

    if (selectedScheme) {
      requestBody.schemes = [selectedScheme];
    }

    try {
      const response = await axios.post(
        BUSINESS_LOGIC_BASE_URL + GET_ABS_RETURN_SOLD,
        requestBody
      );
      const absReturnValue = response.data;
      alert(`Absolute Return for Custom Units: ${absReturnValue}`);
    } catch (error) {
      console.error("Error fetching Absolute Return for Custom Units:", error);
      alert("Error fetching Absolute Return for Custom Units.");
    }
  };

  return (
    <div className="flex flex-col min-h-screen bg-gray-900 p-8">
      <h1 className="text-white text-3xl font-bold mb-8 text-center">
        Calculate Absolute Return
      </h1>

      <div className="max-w-lg mx-auto bg-gray-800 p-6 rounded-lg shadow-md">
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

        <div className="mb-4">
          <label
            className="block text-white text-sm font-semibold mb-2"
            htmlFor="unitsInput"
          >
            Enter Number of Units
          </label>
          <input
            id="unitsInput"
            type="number"
            value={units}
            onChange={handleUnitsChange}
            className="w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded text-sm"
            placeholder="Enter Units"
            required
          />
        </div>

        <div className="overflow-x-auto overflow-y-auto max-h-fit mb-4">
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

        <div className="flex justify-between mb-4 space-x-2">
          <button
            onClick={handleTotalAbsoluteReturn}
            className="flex-grow px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 text-center"
          >
            Total Absolute Return
          </button>

          <button
            onClick={handleBalanceUnitsAbsoluteReturn}
            className="flex-grow px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-center"
          >
            Absolute Return for Balance Units
          </button>

          <button
            onClick={handleCustomUnitsAbsoluteReturn}
            className="flex-grow px-4 py-2 bg-yellow-600 text-white rounded-md hover:bg-yellow-700 text-center"
          >
            Absolute Return for Custom Units
          </button>
          <button
            onClick={handleSoldUnitsAbsoluteReturn}
            className="flex-grow px-4 py-2 bg-yellow-600 text-white rounded-md hover:bg-yellow-700 text-center"
          >
            Absolute Return for Sold Units
          </button>
        </div>
      </div>
    </div>
  );
};

export default AbsoluteReturnScreen;
