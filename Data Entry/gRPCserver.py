import signal
import sys
from concurrent import futures
import grpc
from protos import getNAV_pb2_grpc
from protos import getNAV_pb2


class NAVService(getNAV_pb2_grpc.getNAVServicer):
    def GetData(self, request, context):
        result = 42.0
        return getNAV_pb2.Response(result=result)


def run_grpc_server():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    getNAV_pb2_grpc.add_getNAVServicer_to_server(NAVService(), server)
    server.add_insecure_port("0.0.0.0:3020")
    server.start()
    print("gRPC server is running on port 3020")

    def handle_sigint(signum, frame):
        print("Shutting down the server...")
        server.stop(0)
        sys.exit(0)

    signal.signal(signal.SIGINT, handle_sigint)
    signal.signal(signal.SIGTERM, handle_sigint)

    server.wait_for_termination()
