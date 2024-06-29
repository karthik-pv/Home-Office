from sqlalchemy import (
    Column,
    String,
    PrimaryKeyConstraint,
    Date,
    DOUBLE_PRECISION,
    UniqueConstraint,
    Integer,
)

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


class MasterTable(Base):
    __tablename__ = "MasterTable"
    id = Column(Integer, nullable=False, primary_key=True, autoincrement=True)
    FundHouse = Column(String, nullable=False)
    InvestorName = Column(String, nullable=False)
    TransactionDate = Column(Date, nullable=False)
    TransactionDesc = Column(String, nullable=False)
    Amount = Column(DOUBLE_PRECISION, nullable=True)
    NAV = Column(DOUBLE_PRECISION, nullable=True)
    Load = Column(DOUBLE_PRECISION, nullable=True)
    Units = Column(DOUBLE_PRECISION, nullable=True)
    FundDesc = Column(String, nullable=False)

    __table_args__ = (
        UniqueConstraint(
            "FundHouse",
            "InvestorName",
            "TransactionDate",
            "TransactionDesc",
            "Amount",
            "NAV",
            "Load",
            "Units",
            "FundDesc",
        ),
    )


def getBase():
    return Base
