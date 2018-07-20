pragma solidity ^0.4.22;

contract TestContract{
  address owner;

  constructor() public{
    owner = msg.sender;
  }
  function testTransfer(uint256 pirce) public payable{
    owner.transfer(pirce);
  }

  function getAddr() public view returns(address){
    return owner;
  }
}
