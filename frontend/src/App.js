import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

import HomeScreen from "./screens/HomeScreen";
import NewFundHouseScreen from "./screens/NewFundHouseScreen";
import UploadCsvToFundHouseScreen from "./screens/UploadCSVToFundHouseScreen";
import XirrScreen from "./screens/XirrScreen";

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<HomeScreen />} />
          <Route path="/NewFundHouse" element={<NewFundHouseScreen />} />
          <Route
            path="/ExistingFundHouse"
            element={<UploadCsvToFundHouseScreen />}
          />
          <Route path="/CalculateXirr" element={<XirrScreen />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
