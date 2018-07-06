pragma solidity ^0.4.22;

contract BuyingSellingHouses{

    address owner;
    uint256 startSign;
    uint256 endSign;

    struct HouseInfo{
      string houseToken;
      uint256 sellPrice;
      address sellAddr;
      uint256 commPrice;
      address buyAddr;
      bool isSell;
      bool isCancel;
    }
    mapping(uint256 => HouseInfo) houseSellInfo;

    constructor(uint256 _startSign,uint256 _endSign) public{
      owner = msg.sender;
      startSign = _startSign;
      endSign = _endSign;
    }

    function sellHouse(uint256 _houseid,string _houseToken,uint256 _sellPrice,uint256 _commissionPrice) public {
      require(startSign<=_houseid,"1");
      require(_houseid<=endSign,"2");

      houseSellInfo[_houseid] = HouseInfo(_houseToken,_sellPrice,msg.sender,_commissionPrice,msg.sender,false,false);
    }
    function cancelHouse(uint256 _houseid) public {
      require(startSign<=_houseid,"3");
      require(_houseid<=endSign,"4");
      bool tempIsSell = houseSellInfo[_houseid].isSell;
      require(!tempIsSell,"5");
      houseSellInfo[_houseid].isCancel = true;
    }
    function buyHouse(uint256 _houseid) public payable
        require(startSign<=_houseid,"6");
        require(_houseid<=endSign,"7");

        uint256 tempSellPrice = houseSellInfo[_houseid].sellPrice;
        address tempSellAddr = houseSellInfo[_houseid].sellAddr;
        bool tempIsSell = houseSellInfo[_houseid].isSell;
        bool tempIsCancel = houseSellInfo[_houseid].isCancel;
        require(tempSellAddr != msg.sender,"8");
        require(msg.sender.balance > tempSellPrice,"9");
        require(!tempIsSell,"10");
        require(!tempIsCancel,"11");

        houseSellInfo[_houseid].buyAddr = msg.sender;

        uint256 tempCommissionPrice = houseSellInfo[_houseid].commPrice;
        uint256 tempTruePrice =  tempSellPrice - tempCommissionPrice;
        tempSellAddr.transfer(tempTruePrice);
        owner.transfer(tempCommissionPrice);
        houseSellInfo[_houseid].isSell = true;
    }
}
