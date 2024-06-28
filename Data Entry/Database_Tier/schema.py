from sqlalchemy import Column, String

from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()


class ColumnMapper(Base):
    __tablename__ = "ColumnMapper"
    FundHouse = Column(String, primary_key=True)
    InvestorName = Column(String)
    TransactionDate = Column(String)
    TransactionDesc = Column(String)
    Amount = Column(String)
    NAV = Column(String)
    Load = Column(String)
    Units = Column(String)
    FundDesc = Column(String)
