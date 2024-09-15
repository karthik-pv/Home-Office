from sqlalchemy import (
    Column,
    String,
    PrimaryKeyConstraint,
    Date,
    DOUBLE_PRECISION,
    UniqueConstraint,
    Integer,
    Boolean,
)

from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()


class ColumnMapper(Base):
    __tablename__ = "ColumnMapper"
    fund_house = Column(String, primary_key=True)
    investor_name = Column(String)
    transaction_date = Column(String)
    transaction_desc = Column(String)
    amount = Column(String)
    nav = Column(String)
    load = Column(String)
    units = Column(String)
    bal_units = Column(String)
    fund_desc = Column(String)


class MasterTable(Base):
    __tablename__ = "master_table"
    id = Column(Integer, nullable=False, primary_key=True, autoincrement=True)
    fund_house = Column(String, nullable=False)
    investor_name = Column(String, nullable=False)
    transaction_date = Column(Date, nullable=False)
    transaction_desc = Column(String, nullable=False)
    amount = Column(DOUBLE_PRECISION, nullable=True)
    nav = Column(DOUBLE_PRECISION, nullable=True)
    load = Column(DOUBLE_PRECISION, nullable=True)
    units = Column(DOUBLE_PRECISION, nullable=True)
    bal_units = Column(DOUBLE_PRECISION, nullable=True)
    fund_desc = Column(String, nullable=False)
    net_transaction_amt = Column(DOUBLE_PRECISION, nullable=False)
    status = Column(Boolean, nullable=False)

    __table_args__ = (
        UniqueConstraint(
            "fund_house",
            "investor_name",
            "transaction_date",
            "transaction_desc",
            "amount",
            "nav",
            "load",
            "units",
            "bal_units",
            "fund_desc",
        ),
    )


class TransactionRelevance(Base):
    __tablename__ = "TransactionRelevanceTable"
    id = Column(Integer, nullable=False, primary_key=True, autoincrement=True)
    FundhouseName = Column(String, nullable=False)
    TransactionDesc = Column(String, nullable=False)
    Offset = Column(Integer, nullable=False)


def getBase():
    return Base
