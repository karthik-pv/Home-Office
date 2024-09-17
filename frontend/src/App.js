import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

import HomeScreen from "./screens/HomeScreen";
import NewFundHouseScreen from "./screens/NewFundHouseScreen";
import UploadCsvToFundHouseScreen from "./screens/UploadCSVToFundHouseScreen";
import XirrScreen from "./screens/XirrScreen";
import AbsoluteReturnScreen from "./screens/AbsoluteReturnScreen";

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
          <Route
            path="/CalculateAbsoluteReturn"
            element={<AbsoluteReturnScreen />}
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
