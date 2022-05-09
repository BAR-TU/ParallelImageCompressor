import React from "react";

import Card from "react-bootstrap/Card";

class imageCompressor extends React.Component {
  constructor() {
    super();
    this.state = {
      compressedLink:
        "https://cdn.pixabay.com/photo/2019/03/28/22/22/download-4088189_960_720.png",
      originalImage: "",
      originalLink: "",
      clicked: false,
      uploadImage: false,
      file: [null]
    };
    this.uploadMultipleFiles = this.uploadMultipleFiles.bind(this)
    this.uploadFiles = this.uploadFiles.bind(this)
  }

  fileObj = [];
  fileArray = [];

  callApiLossless() {
    fetch('http://localhost:8080/api/compress/lossless', { method: 'GET' })
        .then(data => console.log(data))
  }

  callApiLossy() {
    fetch('http://localhost:8080/api/compress/lossy', { method: 'GET' })
        .then(data => data.json())
  }

  uploadMultipleFiles(e) {
    this.fileObj.push(e.target.files)
    for (let i = 0; i < this.fileObj[0].length; i++) {
      this.fileArray.push(URL.createObjectURL(this.fileObj[0][i]))
    }
    const imageFile = e.target.files[0];
    this.setState({ file: this.fileArray,
      originalLink: URL.createObjectURL(imageFile),
      originalImage: imageFile,
      outputFileName: imageFile.name,
      uploadImage: true})
  }

  uploadFiles(e) {
    e.preventDefault()
    console.log(this.state.file)
  }

  handle = e => {
    const imageFile = e.target.files[0];
    this.setState({
      originalLink: URL.createObjectURL(imageFile),
      originalImage: imageFile,
      outputFileName: imageFile.name,
      uploadImage: true
    });
  };

  changeValue = e => {
    this.setState({ [e.target.name]: e.target.value });
  };

  click = e => {
    e.preventDefault();
    var el = document.getElementById("optionsCompress");
    var option = el.value;
    if(option==="lossless") {
      this.callApiLossless();
    }
    else if(option==="lossy") {
      this.callApiLossy();
    }

    // const options = {
    //   maxSizeMB: 2,
    //   maxWidthOrHeight: 800,
    //   useWebWorker: true
    // };
    //
    // if (options.maxSizeMB >= this.state.originalImage.size / 1024) {
    //   alert("Bring a bigger image");
    //   return 0;
    // }

   // let output;
    // imageCompression(this.state.originalImage, options).then(x => {
    //   output = x;
    //
    //   const downloadLink = URL.createObjectURL(output);
    //   this.setState({
    //     compressedLink: downloadLink
    //   });
    // });

    this.setState({ clicked: true });
    return 1;
  };

  render() {
    return (
      <div className="m-5">
        <div className="text-black text-center">
          <h1>Image Compressor</h1>
          <h5>Upload one or multiple photos to compress</h5>
        </div>

        <div className="row mt-5 w-70 p-3 max-w-sm bg-white rounded-lg border border-gray-200 shadow-md dark:bg-gray-800 dark:border-gray-700">
          <div className="col-xl-4 col-lg-4 col-md-12 col-sm-12">
            {this.state.uploadImage ? (
              <Card.Img
                  className="ht"
                variant="top"
                src={this.state.originalLink}
              ></Card.Img>
            ) : (
              <Card.Img
                className="ht"
                variant="top"
                src="https://cdn.pixabay.com/photo/2016/01/03/00/43/upload-1118929_1280.png"
              ></Card.Img>
            )}
            <div className="d-flex justify-content-center">
              <input
                type="file"
                accept="image/*"
                className="form-control mt-2 btn btn-dark w-75"
                onChange={this.uploadMultipleFiles} multiple
              />
            </div>
            <h4 className="mt-20">Compression options:</h4>
            Select compression quality:
            <select id="optionsCompress" className="form-select form-select-lg">
              <option value="lossless">Lossless compression</option>
              <option value="lossy">Lossy compression</option>
            </select>
          </div>

          <div className="col-xl-4 col-lg-4 col-md-12 mb-5 mt-5 col-sm-12 d-flex justify-content-center align-items-baseline">
            <br />
            {this.state.outputFileName ? (
              <button
                type="button"
                className=" btn btn-dark"
                onClick={e => this.click(e)}
              >
                Compress
              </button>
            ) : (
              <></>
            )}
          </div>

          <div className="col-xl-4 col-lg-4 col-md-12 col-sm-12 mt-3">
            <Card.Img variant="top" src={this.state.compressedLink}></Card.Img>
            {this.state.clicked ? (
              <div className="d-flex justify-content-center">
                <a
                  href={this.state.compressedLink}
                  download={this.state.outputFileName}
                  className="mt-2 btn btn-dark w-75"
                >
                  Download
                </a>
              </div>
            ) : (
              <></>
            )}
          </div>
        </div>
      </div>
    );
  }
}

export default imageCompressor;
