import grpc
from concurrent import futures
from proto import GetNAV_pb2
from proto import GetNAV_pb2_grpc


class Greeter(GetNAV_pb2_grpc.getNAVServicer):
    def getData(self, request, context):
        return GetNAV_pb2.getData(42.0)


def serveGrpc():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    GetNAV_pb2_grpc.add_getNAVServicer_to_server(Greeter(), server)
    server.add_insecure_port("[::]:50051")
    print("gRPC Server started, listening on port 50051")
    server.start()
    server.wait_for_termination()
