import React from "react";
import { Link } from "react-router-dom";

const HomeScreen = () => {
  return (
    <div className="flex flex-col min-h-screen bg-gray-900">
      <header className="py-4">
        <h1 className="text-white text-5xl font-bold text-center">
          Mutual Fund Home Office
        </h1>
      </header>

      <div className="flex-grow flex items-center justify-center ">
        <div className="flex w-full max-w-5xl">
          <Link to="/NewFundHouse" className="flex-1 mx-2">
            <button className="w-full h-full px-5 py-3 bg-gray-200 text-black border border-gray-500 rounded shadow-md text-xl font-bold hover:bg-gray-300">
              Add New Fundhouse
            </button>
          </Link>
          <Link to="/ExistingFundHouse" className="flex-1 mx-2">
            <button className="w-full h-full px-5 py-3 bg-gray-200 text-black border border-gray-500 rounded shadow-md text-xl font-bold hover:bg-gray-300">
              Upload CSV to Existing Fundhouse
            </button>
          </Link>
          <Link to="/addSchemeMapperData" className="flex-1 mx-2">
            <button className="w-full h-full px-5 py-3 bg-gray-200 text-black border border-gray-500 rounded shadow-md text-xl font-bold hover:bg-gray-300">
              Update Scheme Map
            </button>
          </Link>
          <Link to="/CalculateXIRR" className="flex-1 mx-2">
            <button className="w-full h-full px-5 py-3 bg-gray-200 text-black border border-gray-500 rounded shadow-md text-xl font-bold hover:bg-gray-300">
              Calculate XIRR
            </button>
          </Link>
          <Link to="/CalculateAbsoluteReturn" className="flex-1 mx-2">
            <button className="w-full h-full px-5 py-3 bg-gray-200 text-black border border-gray-500 rounded shadow-md text-xl font-bold hover:bg-gray-300">
              Calculate Absolute Return
            </button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default HomeScreen;
