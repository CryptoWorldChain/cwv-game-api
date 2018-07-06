pragma solidity ^0.4.22;

contract GamePlay {
  struct slice {
      uint _len;
      uint _ptr;
  }
  //中奖号码
  struct WinNumValue{
    string period;//期号
    string winOne;
    string winTwo;
    string winThree;
    string[] winArray;
  }

  //用户投注信息
  struct BettingValue{
    string period;//期号
    string bettOne;
    string bettTwo;
    string playtype;//投注玩法
    string chipintype;//投注方式
    string multiple;//倍数
    string[] bettingArray;
    string bettBool;//投注号码为空
    string level;//奖等
    string money;//金额
  }

mapping(address => BettingValue[]) userBettingInfo;
mapping(address => uint)  checkUser;
//用户投注地址集合
address[] userBetAddr = new address[](1);
//奖池
address pooladdr;
//销售金额
address saleaddr;
//开启地址
address owner;
//佣金地址
address commission;
//期号
string period;
//当前期号
string currentPeriod;
//开奖号码
string winCode;

string divisionNameInfo;
//开始时间
uint actionStart;
//结束时间
uint auctionEnd;
//是否算奖
bool isComputerPrize = false;
//开奖号码存储
WinNumValue winNumValue;
//设置合约创建者
constructor() public{
  owner = msg.sender;
}

//设置地址信息
/*
_pooladdr 奖池地址，存储初始值，及追加奖池金额
_saleaddr 销售金额存储地址
_commission 佣金提取地址  暂定义成传入方式
_currentPeriod 当前期号
*/
function setInfo(address _pooladdr,address _saleaddr,
  address _commission,string _currentPeriod) public payable{
    //不是创建合约用户，不能设置地址信息
    require(owner==msg.sender);
    pooladdr = _pooladdr;
    saleaddr = _saleaddr;
    commission = _commission;
    currentPeriod = _currentPeriod;
    auctionEnd = now + 60*5*1000;//pk10游戏间隔5分钟  设置成默认5分钟
}

//奖池增加金额
function addPool(uint256 _poolAmount) public payable{
  //不是创建合约用户，不能设置奖池资金
  require(owner==msg.sender);
  pooladdr.transfer(_poolAmount);
}
event testString(string codestr);
//设置开奖号码和开奖期号
function setGamePeriod(string _winCode,string _period) public{
    //不是创建合约用户，不能设置开奖号码和开奖期号
    require(owner==msg.sender);

    period = _period;
    winCode = _winCode;


    slice memory delim0 = toSlice("|");

    slice memory wagerdataArraystr = toSlice(_winCode);
    string[] memory wagerdataArray = new string[](count(wagerdataArraystr,delim0) + 1);

    for(uint aa=0;aa<wagerdataArray.length;aa++){
      wagerdataArray[aa] = toString(split(wagerdataArraystr,delim0));
    }
    winNumValue = WinNumValue(_period,wagerdataArray[0],wagerdataArray[1],
      wagerdataArray[2],wagerdataArray);

}

/*投注信息
_strBet   投注号码格式：“玩法:投注方式:投注号码1*倍数,投注号码2*倍数”
"1,1:1,1:08|03|02|10|06|04|09|07|05|01*5,01|02|03|06|04|10|08|07|09|05*10"
投注号码格式：“玩法|投注方式|倍数|投注号码(10个)|1表示有投注信息，0表示没有”
"1|1|10|01|02|03|06|04|10|08|07|09|05|1"
"4|1|10|00|00|00|00|00|00|00|00|00|00|0"

*/
function userBetting(string _strBet,string _period) public payable{
  require(now < auctionEnd);
  //检查用户是否存在，如不存在，则加入用户地址数组；10表示已经投注过
  if(checkUser[msg.sender]==0){
    checkUser[msg.sender] = 10;
    userBetAddr.push(msg.sender);
  }
  //销售金额累加
  saleaddr.transfer(msg.value);
  //减去投注的金额
  msg.sender.transfer(-msg.value);
  //加入投注信息
  slice memory delim0 = toSlice("|");
    slice memory wagerdataArraystr = toSlice(_strBet);
    string[] memory wagerdataArray = new string[](count(wagerdataArraystr,delim0) + 1);
    for(uint aa=0;aa<wagerdataArray.length;aa++){
      wagerdataArray[aa] = toString(split(wagerdataArraystr,delim0));
    }

  string[] memory tempArray = new string[](10);
  tempArray[0] = wagerdataArray[3];
  tempArray[1] = wagerdataArray[4];
  tempArray[2] = wagerdataArray[5];
  tempArray[3] = wagerdataArray[6];
  tempArray[4] = wagerdataArray[7];
  tempArray[5] = wagerdataArray[8];
  tempArray[6] = wagerdataArray[9];
  tempArray[7] = wagerdataArray[10];
  tempArray[8] = wagerdataArray[11];
  tempArray[9] = wagerdataArray[12];
  userBettingInfo[msg.sender].push(BettingValue(_period,wagerdataArray[0],wagerdataArray[1],
    wagerdataArray[2],wagerdataArray[4],wagerdataArray[5],tempArray,wagerdataArray[13],"0","0"));
}

//算奖
function computerPrize() public payable{

  //false表示没有算过奖
  require(!isComputerPrize);
  //不是创建合约用户，不能算奖
  require(owner==msg.sender);
  require(now >= auctionEnd);

  //require(gameCode);
  //计算
  //循环投注数组，判断是否中奖
  for(uint m=0;m<userBetAddr.length;m++){
      address tempaddr = userBetAddr[m];
      BettingValue[] memory tempUser = userBettingInfo[tempaddr];
      uint256 award = 0;
      for(uint n=0;n<tempUser.length;n++){
          BettingValue memory tempUserInfo = tempUser[n];
          string memory divisionType = calcPrizeLevelStruct(tempUserInfo);
          string memory prizeLevel = divisionTransfer(divisionType);
          string memory prizeMoney0 = divisionMoney(prizeLevel);

          tempUser[n].level = prizeLevel;
          tempUser[n].money = prizeMoney0;
          uint multiple = parseInt(tempUserInfo.multiple);
          award = award + parseInt(prizeMoney0)*multiple;
      }
      //算奖同时返奖
      if(award == 0){
        tempaddr.transfer(award);
      }
  }

  //算过奖后值为true
  isComputerPrize = true;
}



function divisionMoney(string infoLevel) internal pure returns(string){
  string memory prizeMoney;
  if(stringsEqual("55",infoLevel)){
    prizeMoney = "44";
  }else if(stringsEqual("56",infoLevel)){
    prizeMoney = "12";
  }else if(stringsEqual("57",infoLevel)){
    prizeMoney = "12";
  }else if(stringsEqual("58",infoLevel)){
    prizeMoney = "5";
  }else if(stringsEqual("59",infoLevel)){
    prizeMoney = "5";
  }else{
    prizeMoney = "0";
  }
  return prizeMoney;
}

//获取奖等及奖金
function divisionTransfer(string infoName) internal pure returns (string){
  string memory prizeLevel;
  //普通投注
  if(stringsEqual("4_1_0",infoName)){
    prizeLevel = "55";
  }else if(stringsEqual("5_1_0",infoName)){
    prizeLevel = "56";
  }else if(stringsEqual("5_2_0",infoName)){
    prizeLevel = "57";
  }else if(stringsEqual("6_1_0",infoName)){
    prizeLevel = "58";
  }else if(stringsEqual("6_2_0",infoName)){
    prizeLevel = "59";
  }
  return prizeLevel;
}


/*
计算奖等并返奖
*/

function calcPrizeLevelStruct (BettingValue memory bettingValue) internal view returns(string){


  //玩法
  string memory bettypeArr = bettingValue.playtype;
  string memory divisionName0;

  //倍数在
  //string memory betmultirArr = bettingValue.multiple;
  //4拖拉机投注 1  01|02|03*1
  //5猜奇偶  全奇1 01|02|03*1  全偶2 01|02|03*1
  //6猜大小  猜大1 01|02|03*1  猜小2  01|02|03*1
  if (stringsEqual("4",bettypeArr) || stringsEqual("5",bettypeArr)|| stringsEqual("6",bettypeArr)) {
    //divisionName0 = getDivisionName("", winnumber, bettypeArr, chipintypeArr);
    divisionName0 = getDivisionName(bettingValue);
  }
  return divisionName0;
}

function getDivisionName(BettingValue bettingValue) internal view returns (string){
  string memory divisionName;
  if(stringsEqual("4",bettingValue.playtype)&&stringsEqual("1",bettingValue.chipintype)){
    //拖拉机投注
    divisionName = computer4And1(bettingValue);
  }else if(stringsEqual("5",bettingValue.playtype)){
    //猜奇偶
    divisionName = computer5(bettingValue);
  }else if(stringsEqual("6",bettingValue.playtype)){
    //猜大小
    divisionName = computer6(bettingValue);
  }
  return divisionName;

}


function computer4And1(BettingValue bettingValue)  internal view returns (string){
  string memory divisionName4;
  uint n1 = parseInt(winNumValue.winOne);
  uint n2 = parseInt(winNumValue.winTwo);
  uint n3 = parseInt(winNumValue.winThree);
  if ((n1 + 1 == n2 && n2 + 1 == n3) || (n1 - 1 == n2 && n2 - 1 == n3)) {
    // 拖拉机,wagerdata固定位：1,2,3
    divisionName4 = strConcat(bettingValue.playtype , "_" , bettingValue.chipintype , "_0","");
  }
  return divisionName4;
}
function computer5(BettingValue bettingValue)  internal view returns (string){
  string memory divisionName5;
  uint n11 = parseInt(winNumValue.winOne);
  uint n21 = parseInt(winNumValue.winTwo);
  uint n31 = parseInt(winNumValue.winThree);
  if (stringsEqual("1",bettingValue.chipintype) && n11 % 2 != 0 && n21 % 2 != 0 && n31 % 2 != 0) {
    // 全奇(1),wagerdata固定位：1,2,3
    divisionName5 = strConcat(bettingValue.playtype , "_" , bettingValue.chipintype , "_0","");
  } else if (stringsEqual("2",bettingValue.chipintype) && n11 % 2 == 0 && n21 % 2 == 0 && n31 % 2 == 0) {
    // 是全偶(2),wagerdata固定位：1,2,3
    divisionName5 = strConcat(bettingValue.playtype , "_" , bettingValue.chipintype , "_0","");
  }
  return divisionName5;
}
function computer6(BettingValue bettingValue)  internal view returns (string){
  string memory divisionName6;
  uint n12 = parseInt(winNumValue.winOne);
  uint n22 = parseInt(winNumValue.winTwo);
  uint n32 = parseInt(winNumValue.winThree);
  uint n = n12 + n22 + n32;
  if (stringsEqual("1",bettingValue.chipintype) && 21 <= n && n <= 27) {
    // 猜大(1),wagerdata固定位：1,2,3
    divisionName6 = strConcat(bettingValue.playtype , "_" , bettingValue.chipintype , "_0","");
  } else if (stringsEqual("2",bettingValue.chipintype) && 6 <= n && n <= 12) {
    // 猜小(2),wagerdata固定位：1,2,3
    divisionName6 = strConcat(bettingValue.playtype , "_" , bettingValue.chipintype , "_0","");
  }
  return divisionName6;
}



function nameLength(string name) internal pure returns (uint) {
    return bytes(name).length;
}
//字符串转整数
function parseInt(string _a) internal pure returns (uint) {
    return parseInt(_a, 0);
}

//字符串转整数
function parseInt(string _a, uint _b) internal pure returns (uint) {
    bytes memory bresult = bytes(_a);
    uint mint = 0;
    bool decimals = false;
    for (uint i=0; i<bresult.length; i++){
        if ((bresult[i] >= 48)&&(bresult[i] <= 57)){
            if (decimals){
               if (_b == 0) break;
                else _b--;
            }
            mint *= 10;
            mint += uint(bresult[i]) - 48;
        } else if (bresult[i] == 46) decimals = true;
    }
    if (_b > 0) mint *= 10**_b;
    return mint;
}
//字符串相等
function stringsEqual(string memory _a, string memory _b) internal pure returns (bool) {
    bytes memory a = bytes(_a);
    bytes memory b = bytes(_b);
    if (a.length != b.length)
      return false;
    // @todo unroll this loop
    for (uint i = 0; i < a.length; i ++)
      if (a[i] != b[i])
        return false;
    return true;
  }
  //字符串连接
  function strConcat(string _a, string _b, string _c, string _d, string _e) internal pure returns (string){
    bytes memory _ba = bytes(_a);
    bytes memory _bb = bytes(_b);
    bytes memory _bc = bytes(_c);
    bytes memory _bd = bytes(_d);
    bytes memory _be = bytes(_e);
    string memory abcde = new string(_ba.length + _bb.length + _bc.length + _bd.length + _be.length);
    bytes memory babcde = bytes(abcde);
    uint k = 0;
    for (uint i = 0; i < _ba.length; i++) babcde[k++] = _ba[i];
    for (i = 0; i < _bb.length; i++) babcde[k++] = _bb[i];
    for (i = 0; i < _bc.length; i++) babcde[k++] = _bc[i];
    for (i = 0; i < _bd.length; i++) babcde[k++] = _bd[i];
    for (i = 0; i < _be.length; i++) babcde[k++] = _be[i];
    return string(babcde);
  }

  //数字转bytes
  function toBytes(uint256 x)internal pure returns (bytes) {
    bytes memory b = new bytes(32);
    assembly { mstore(add(b, 32), x) }
  }


  function toSlice(string self) internal pure returns (slice) {
      uint ptr;
      assembly {
          ptr := add(self, 0x20)
      }
      return slice(bytes(self).length, ptr);
  }

  function split(slice self, slice needle, slice token) internal pure returns (slice) {
      uint ptr = findPtr(self._len, self._ptr, needle._len, needle._ptr);
      token._ptr = self._ptr;
      token._len = ptr - self._ptr;
      if (ptr == self._ptr + self._len) {
          // Not found
          self._len = 0;
      } else {
          self._len -= token._len + needle._len;
          self._ptr = ptr + needle._len;
      }
      return token;
  }

  function split(slice self, slice needle) internal pure returns (slice token) {
        split(self, needle, token);
    }

  function findPtr(uint selflen, uint selfptr, uint needlelen, uint needleptr) private pure returns (uint) {
      uint ptr = selfptr;
      uint idx;

      if (needlelen <= selflen) {
          if (needlelen <= 32) {
              bytes32 mask = bytes32(~(2 ** (8 * (32 - needlelen)) - 1));

              bytes32 needledata;
              assembly { needledata := and(mload(needleptr), mask) }

              uint end = selfptr + selflen - needlelen;
              bytes32 ptrdata;
              assembly { ptrdata := and(mload(ptr), mask) }

              while (ptrdata != needledata) {
                  if (ptr >= end)
                      return selfptr + selflen;
                  ptr++;
                  assembly { ptrdata := and(mload(ptr), mask) }
              }
              return ptr;
          } else {
              // For long needles, use hashing
              bytes32 hash;
              assembly { hash := sha3(needleptr, needlelen) }

              for (idx = 0; idx <= selflen - needlelen; idx++) {
                  bytes32 testHash;
                  assembly { testHash := sha3(ptr, needlelen) }
                  if (hash == testHash)
                      return ptr;
                  ptr += 1;
              }
          }
      }
      return selfptr + selflen;
  }

  function count(slice self, slice needle) internal pure returns (uint cnt) {
        uint ptr = findPtr(self._len, self._ptr, needle._len, needle._ptr) + needle._len;
        while (ptr <= self._ptr + self._len) {
            cnt++;
            ptr = findPtr(self._len - (ptr - self._ptr), ptr, needle._len, needle._ptr) + needle._len;
        }
    }

    function toString(slice self) internal pure returns (string) {
        string memory ret = new string(self._len);
        uint retptr;
        assembly { retptr := add(ret, 32) }

        memcpy(retptr, self._ptr, self._len);
        return ret;
    }

    function memcpy(uint dest, uint src, uint len) private pure {
        // Copy word-length chunks while possible
        for(; len >= 32; len -= 32) {
            assembly {
                mstore(dest, mload(src))
            }
            dest += 32;
            src += 32;
        }

        // Copy remaining bytes
        uint mask = 256 ** (32 - len) - 1;
        assembly {
            let srcpart := and(mload(src), not(mask))
            let destpart := and(mload(dest), mask)
            mstore(dest, or(destpart, srcpart))
        }
    }

}
