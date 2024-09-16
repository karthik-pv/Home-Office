import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

import HomeScreen from "./screens/HomeScreen";
import NewFundHouseScreen from "./screens/NewFundHouseScreen";

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<HomeScreen />} />
          <Route path="/NewFundHouse" element={<NewFundHouseScreen />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
