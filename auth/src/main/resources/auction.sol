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
      bidStartingPrice = _bidStartingPrice;
      increasePrice = _increasePrice;
      startTime = _startTime;
      endTime = _endTime;
      tokenID = _tokenID;
      blocklow = block.number + 10;
      blockMax = block.number + 100;
      /* blocklow = _blocklow; */
      /* blockMax = _blockMax; */
    }
    event testData(uint256 num,address tempaddrs);
    /* event test(uint256 num,address tempaddrs); */
    function auctionBid(uint256 _bigPrice)  public payable{
      require(beneficiary != msg.sender,"1");

      require(block.number > blocklow,"2");

      require(block.number < blockMax,"3");
      require(highestBid < _bigPrice,"4");
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
      }else{
        computerBidPrice = _bigPrice;
        if(highestBidder>0){
            differencePrice = _bigPrice - bidStartingPrice;
        }else{
            differencePrice = _bigPrice - highestBid;
        }
        tempBidFrequency = 1;
        auctionUser.push(msg.sender);
      }
      require(msg.sender.balance >= computerBidPrice,"5");

      require(differencePrice >= increasePrice,"6");

      highestBid = _bigPrice;
      highestBidder = msg.sender;
      beneficiary.transfer(computerBidPrice);
      pendingReturns[msg.sender] = AuctionInfo(_bigPrice,tempBidFrequency,true);

    }


    function auctionEnd() public payable{
      require(msg.sender == beneficiary,"7");

      require(block.number > blockMax,"8");

      require(!ended,"9");
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

    }

    function getMaxPrice() public view returns(uint256){
        return highestBid;
    }
    function getMaxAddress() public view returns(address){
        return highestBidder;
    }

    function getTokenID() public view returns(string){
        return tokenID;
    }


}
