pragma solidity ^0.4.22;

contract Auction{
    bool ended = false;
    uint256 startTime;
    uint256 endTime;
    address beneficiary;
    address highestBidder;
    uint256 highestBid = 0;
    uint256 blocklow;
    uint256 blockMax;
    uint256 blockTime;
    uint256 currentTime;
    uint256 currentBlockNum;

    uint256 bidStartingPrice;
    uint256 increasePrice;
    string tokenID;

    struct AuctionInfo{
      uint256 bidPrice;
      uint256 bidFrequency;
      bool isExist;
    }
    address[] auctionUser = new address[](1);
    mapping(address => AuctionInfo) pendingReturns;


    constructor(string _tokenID,uint256 _startTime,
      uint256 _endTime,uint256 _blockTime,uint256 _bidStartingPrice,
      uint256 _increasePrice) public {
      beneficiary = msg.sender;
      currentTime = now;
      currentBlockNum = block.number;
      bidStartingPrice = _bidStartingPrice;
      increasePrice = _increasePrice;
      startTime = _startTime;
      endTime = _endTime;
      tokenID = _tokenID;
      blockTime = _blockTime;
      if(startTime <endTime){
          uint256 tempStart = 0;
          if(startTime>currentTime){
            tempStart = startTime - currentTime;
          }

          uint256 tempEnd = endTime - currentTime;

          uint256 tempStartNum = tempStart/blockTime;
          uint256 tempEndNum = tempEnd/blockTime;

          blocklow = currentBlockNum + tempStartNum;
          blockMax = currentBlockNum + tempEndNum;
      }
    }
    event testData(uint256 num,address tempaddrs);

      function auctionBid(uint256 _bigPrice)  public payable returns(uint256){
      /* require(beneficiary != msg.sender,"1");

      require(block.number >= blocklow,"2");

      require(block.number <= blockMax,"3");
      require(highestBid < _bigPrice,"4"); */

      if(beneficiary != msg.sender){
        if(block.number >= blocklow){
            if(block.number <= blockMax){
                if(highestBid < _bigPrice){
                  uint256 differencePrice;
                  uint256 computerBidPrice;
                  uint256 tempBidFrequency;

                  if(pendingReturns[msg.sender].isExist){
                    AuctionInfo storage auctionInfo = pendingReturns[msg.sender];
                    uint256 tempbidPrice = auctionInfo.bidPrice;
                    differencePrice = _bigPrice - tempbidPrice;

                    computerBidPrice = differencePrice;

                    tempBidFrequency = auctionInfo.bidFrequency;
                    tempBidFrequency = tempBidFrequency + 1;

                    pendingReturns[msg.sender] = AuctionInfo(_bigPrice,tempBidFrequency,true);
                  }else{
                    computerBidPrice = _bigPrice;

                    differencePrice = _bigPrice - highestBid;

                    tempBidFrequency = 1;
                    pendingReturns[msg.sender] = AuctionInfo(_bigPrice,tempBidFrequency,true);
                    auctionUser.push(msg.sender);
                  }
                  /* require(msg.sender.balance >= computerBidPrice,"5");

                  require(differencePrice >= increasePrice,"6"); */
                  if(msg.sender.balance >= computerBidPrice){
                    if(differencePrice >= increasePrice){
                      highestBid = _bigPrice;
                      highestBidder = msg.sender;

                      beneficiary.transfer(computerBidPrice);


                      return 0;
                    }else{
                        return 6;
                    }
                  }else{
                      return 5;
                  }
                }else{
                    return 4;
                }
            }else{
                return 3;
            }
        }else{
            return 2;
        }
      }else{
          return 1;
      }

    }


    function auctionEnd() public payable returns(uint256){
      /* require(msg.sender == beneficiary,"7");

      require(block.number > blockMax,"8");

      require(!ended,"9"); */
      if(msg.sender == beneficiary){
          if(block.number > blockMax){
            if(!ended){
              ended = true;

              uint256 len = auctionUser.length;
              for(uint256 i=1;i<len;i++){
                  address tempUserAddr = auctionUser[i];
                  if(tempUserAddr != highestBidder){
                    AuctionInfo storage auctionInfo = pendingReturns[tempUserAddr];
                    uint256 tempPrice = auctionInfo.bidPrice;
                    tempUserAddr.transfer(tempPrice);
                  }
              }
              return 0;
            }else{
              return 9;
            }
          }else{
            return 8;
          }
      }else{
          return 7;
      }

    }
    /* function strConcat(string _a, string _b, string _c, string _d, string _e) internal pure returns (string){
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
  function ToString(address x) internal pure returns (string) {
    bytes memory b = new bytes(20);
    for (uint i = 0; i < 20; i++)
        b[i] = byte(uint8(uint(x) / (2**(8*(19 - i)))));
    return string(b);
    }
    function convert(uint256 n) internal pure returns (bytes32) {
        return bytes32(n);
    }
    function bytes32ToString (bytes32 data) internal pure returns (string) {
        bytes memory bytesString = new bytes(32);
        for (uint j=0; j<32; j++) {
            byte char = byte(bytes32(uint(data) * 2 ** (8 * j)));
            if (char != 0) {
                bytesString[j] = char;
            }
        }
        return string(bytesString);
    }
    function getMaxPriceAndAddress()public view returns(string){
        return strConcat(bytes32ToString(convert(highestBid)),";",ToString(highestBidder),"","");
    } */

    function getMaxPrice() public view returns(uint256){
        return highestBid;
    }
    function getMaxAddress() public view returns(address){
        return highestBidder;
    }
    function getBeneficiary() public view returns(address){
        return beneficiary;
    }
    function getMsgSender() public view returns(address){
        return msg.sender;
    }
    function getblocknumber() public view returns(uint256){
        return block.number;
    }

    function getblockMax() public view returns(uint256){
        return blockMax;
    }
    function getblocklow() public view returns(uint256){
        return blocklow;
    }
    function getcurrentTime() public view returns(uint256){
        return currentTime;
    }
    function getstartTime() public view returns(uint256){
        return startTime;
    }
    function getendTime() public view returns(uint256){
        return endTime;
    }
    function getTokenID() public view returns(string){
        return tokenID;
    }


}
