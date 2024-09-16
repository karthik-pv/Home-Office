package dev.jojo.HomeOffice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("home-office")
public class HomeOfficeController {

    @Autowired
    private HomeOfficeService serviceObj;

    @GetMapping("test")
    public String test(){
        return "here";
    }

    @PostMapping("absoluteReturn")
    public double calculateAbsoluteCost(@RequestBody AbsoluteReturn data){
        double marketValue = data.getMarketValue();
        double totalCost = data.getTotalCost();
        return serviceObj.calculateAbsoluteReturn(marketValue,totalCost);
    }

    @PostMapping("xirr")
    public double calculateXirr(@RequestBody String jsonData){
        return serviceObj.calculateXirr(jsonData);
    }
}
