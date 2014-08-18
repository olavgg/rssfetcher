from flask import Flask, send_from_directory, request, Response
from flask import stream_with_context
import requests
app = Flask(__name__, static_url_path='',static_folder='html')

@app.route('/')
def root():
    return send_from_directory(app.static_folder, 'index.html')

@app.route('/style.css')
@app.route('/font-awesome.min.css')
@app.route('/fontawesome-webfont.woff')
def static_files():
    return send_from_directory(app.static_folder, request.path[1:])

@app.route('/esget/<path:url>')
def elasticsearch_get(url):
    req = requests.get(url, stream = True)
    return Response(
        stream_with_context(req.iter_content()),
        content_type = req.headers['content-type'])

@app.route('/espost/<path:url>', methods=["POST"])
def elasticsearch_post(url):
    req = requests.post(url, stream = True)
    return Response(
        stream_with_context(req.iter_content()),
        content_type = req.headers['content-type'])

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)
