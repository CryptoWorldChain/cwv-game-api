pragma solidity ^0.4.22;

//总奖池合约
contract PrizePool{
  //可控制资金转出地址
  address owner;
  //大奖池地址
  address[] pools = new address[](1);

  //奖池总金额
  uint256 poolAmount = 0;

  //生成合约时，一次性传入多个奖池地址
  function PrizePool(address poolOne,address poolTwo,address poolThree,address poolFour,address poolFive,
    address poolSix,address poolSeven,address poolEight,address poolNine,address poolTen) public {
        owner = poolOne;
        pools.push(poolOne);
        pools.push(poolTwo);
        pools.push(poolThree);
        pools.push(poolFour);
        pools.push(poolFive);
        pools.push(poolSix);
        pools.push(poolSeven);
        pools.push(poolEight);
        pools.push(poolNine);
        pools.push(poolTen);
        //重新计算
        setPoolAmount();
  }

  //修改owner的控制地址
  function changOwner(address modiAddr) public {
    require(msg.sender == owner);
    owner = modiAddr;
  }
  //扩展奖池
  function poolEx(address newPool) public {
    require(owner == msg.sender);
    if(!checkAddress(newPool)){
      pools.push(newPool);
      //重新计算
      setPoolAmount();
    }

  }

  //获取每个账户的余额，并累加到奖池总金额中
  function setPoolAmount() internal{
    poolAmount = 0;
    for(uint i=0;i<pools.length;i++){
        poolAmount = poolAmount + pools[i].balance;
    }
  }

  //获取奖池总金额
  function getPoolAmount() public returns(uint256){
    return poolAmount;
  }
  /*//给奖池增加资金
  function setPoolMoney(address _addrMoney,uint256 _intMoney) public {
      require(owner == msg.sender);
      require(_intMoney>0);

      poolAmount = poolAmount + _intMoney;
      _addrMoney.transfer(_intMoney);
      //重新计算
      setPoolAmount();
  }*/

  //检查地址是否存在
  function checkAddress(address _form) internal returns(bool){
    bool sign = false;
    for(uint i=0;i<pools.length;i++){
        if(pools[i]==_form){
          sign = true;
          break;
        }
    }
    return sign;
  }

  //转账给指定的奖池
 function transferzMoney(address _form,address _to,uint256 _intMoney)public payable{
   //require(owner == msg.sender);
   require(_intMoney>0);
   if(checkAddress(_form)&&checkAddress(_to)){
       _to.transfer(_intMoney);
   }
 }
}
