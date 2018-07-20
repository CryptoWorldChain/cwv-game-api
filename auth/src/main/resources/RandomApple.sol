pragma solidity ^0.4.22;

contract RandomApple{
uint256 seedTotal = 100000;
uint256 seedNum = 5000;
uint256 rand_seed = 249075560159305;

uint256[] randomNum = new uint256[](1);

address owner;
address[] userAddrArray = new address[](1);

uint256 multiplier = 25214903917;
uint256 addend = 11;
uint256 mask = 281474976710655;


bool isExcuter = false;

uint useNum = 20;
uint256 withdrawalfPrice = 1;

struct RandomUser{
    //address userAddr;
    string fingerprint;
    uint256 minergold;
    bool isUse;
    bool isExist;
}

mapping(address => RandomUser) randomInfo;
event newRandomNumber_uint(uint256);
event userInfo(address,string);
constructor() public{
  owner = msg.sender;
  deleteStrAt(0);
  deleteUserAt(0);
}


function fingerprintInfo(string result) public {
  require(!randomInfo[msg.sender].isExist,"1");
  require(!isExcuter,"2");
  userAddrArray.push(msg.sender);
  randomInfo[msg.sender] = RandomUser(result,0,false,true);
}
function getSize() public view returns(uint256){
  return userAddrArray.length;
}
function getInfo() public view returns(string){
  RandomUser memory temp = randomInfo[msg.sender];
  return temp.fingerprint;
}


function computerRandomNumber() public{
  isExcuter = true;
  require(owner == msg.sender,"3");
  if(randomNum.length==0 && userAddrArray.length < useNum){
      rand_seed = autoRandom("cwv");
      for(uint l=0;l<seedNum;l++){
        uint256 templ = autoRandomSeed();
        emit newRandomNumber_uint(templ);
        randomNum.push(templ);
    }
  }else{
    require(userAddrArray.length >= useNum,"4");

    for(uint i=0;i<useNum;i++){
      address tempUserAddr = userAddrArray[i];
      RandomUser storage tempUser = randomInfo[tempUserAddr];

      emit userInfo(tempUserAddr,tempUser.fingerprint);
      string memory userfinger = tempUser.fingerprint;
      rand_seed = autoRandom(userfinger);
      emit newRandomNumber_uint(rand_seed);

      for(uint k=0;k<seedNum;k++){
          randomNum.push(autoRandomSeed());
      }
    }
    for(uint m=0;m<useNum;m++){
        address delUserAddr = userAddrArray[m];
        //withdrawalfzunds(delUserAddr,withdrawalfPrice);
        deleteUserAt(0);
        delete randomInfo[delUserAddr];
    }
  }
  isExcuter = false;


}
function getFixedRange(uint256 maxNum) public returns (uint256){
    require(randomNum.length>0,"5");
    uint256 numRand = randomNum[0];
    rand_seed = numRand;
    uint256 tempFix = nextInt(maxNum);
    deleteStrAt(0);
    return tempFix;
}

function nextInt(uint256 n) public returns(uint256){

    require(n>0,"6");

    if ((n & -n) == n)  // i.e., n is a power of 2
        return uint256(((n * next(31)) >> 31));

    uint256 bits;
    uint256 val;
    do {
        bits = next(31);
        val = bits % n;
    } while (bits - val + (n-1) < 0);
    return val;
}

function next(uint256 bits) public returns (uint256) {
    uint256 nextseed;

    nextseed = (rand_seed * multiplier + addend) & mask;
    emit newRandomNumber_uint(nextseed);
    return uint256(nextseed >> (48 - bits));
}
function getRandmon() public returns (uint256){
  uint numRand = randomNum[0];
  deleteStrAt(0);
  return numRand;
}

function deleteStrAt(uint256 index) internal{
    uint256 len = randomNum.length;
    if (index >= len) return;
    for (uint i = index; i<len-1; i++) {
      randomNum[i] = randomNum[i+1];
    }
    delete randomNum[len-1];
    randomNum.length--;
}
function deleteUserAt(uint256 index) internal{
    uint256 len = userAddrArray.length;
    if (index >= len) return;
    for (uint i = index; i<len-1; i++) {
      userAddrArray[i] = userAddrArray[i+1];
    }

    delete userAddrArray[len-1];
    userAddrArray.length--;
}

function autoRandom(string _fingerprint) public returns (uint256){
    uint256 randomNumber = uint256(sha256(_fingerprint));
    emit newRandomNumber_uint(randomNumber);
    return randomNumber;
}

function autoRandomSeed() public returns(uint256){
    rand_seed = rand_seed * 1103515245 + 12345;
    uint256 maxRange = 2**(8*7);
    uint256 randomNumber = rand_seed % maxRange;
    emit newRandomNumber_uint(randomNumber);
    return randomNumber;
}

function withdrawalfzunds(address _addr,uint _price) public payable{
    _addr.transfer(_price);
}
}
