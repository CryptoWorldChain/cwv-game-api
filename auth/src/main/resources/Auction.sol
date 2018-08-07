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

      require(block.number > blocklow,"2");

      require(block.number < blockMax,"3");
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
    //   require(msg.sender == beneficiary,"7");

    //   require(block.number > blockMax,"8");

    //   require(!ended,"9");
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
