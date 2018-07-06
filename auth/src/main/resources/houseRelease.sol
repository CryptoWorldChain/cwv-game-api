pragma solidity ^0.4.22;

/*
721token发行
*/
contract HouseRelease{

  //超级账户地址
  address owner;

  //721token
  string symol;
  //名称
  string name;
  //Id
  string code;
  //索引
  string index;
  //总量
  string total;


  constructor() public{
    owner = msg.sender;
  }

  function autoCreateHouse(string[][] houseInfo) public {
    for(uint i=0;i<hoseInfo.length;i++){
      string[] temp = hoseInfo[i];

    }
  }

  function createHouse(string _symol,string _name,string _code,string _index,string _total) public{
    require(owner == msg.sender);
      symol = _symol;
      name = _name;
      code = _code;
      index = _index;
      total = _total;
  }

}
