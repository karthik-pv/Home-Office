import grpc
from concurrent import futures
from proto import get_nav_pb2
from proto import get_nav_pb2_grpc


class Greeter(get_nav_pb2_grpc.get_navServicer):
    def GetData(self, request, context):
        print(f"Received query: {request.query}")

        # Validate request
        if not request.query:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Query cannot be empty.")
            return get_nav_pb2.Response()

        # Mock processing logic for the query
        try:
            # This is where you would implement your logic to process the query.
            # For demonstration, we're returning a fixed value.
            result = self.process_query(request.query)
            return get_nav_pb2.Response(result=result)

        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(f"An error occurred: {str(e)}")
            return get_nav_pb2.Response()

    def process_query(self, query):
        # Simulate some processing based on the query, replace with actual logic
        # Here we return a fixed number as a dummy result
        # Implement your actual query processing here
        if query == "foo":
            return 42.0
        elif query == "bar":
            return 84.0
        else:
            raise ValueError("Unsupported query")


def serveGrpc():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    get_nav_pb2_grpc.add_get_navServicer_to_server(Greeter(), server)
    server.add_insecure_port("[::]:50051")
    print("gRPC Server started, listening on port 50051")
    server.start()
    server.wait_for_termination()
