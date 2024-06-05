"use client";
import { useEffect, useState } from "react";

interface FunctionData {
  [key: string]: string[];
}

const Page = () => {
  const [data, setData] = useState<FunctionData | null>(null);
  const [fileData, setFileData] = useState<string>("");
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [fileSelected, setFileSelected] = useState<boolean>(false);

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const response = await fetch(
        "http://localhost:8080/api/analyze?" +
          new URLSearchParams({
            file: fileData,
          })
      );
      const jsonData: FunctionData = await response.json();
      setData(jsonData);
    } catch (error) {
      console.error("Error fetching data:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleFileInputEvent = async (ev: any) => {
    const file = ev.target.files.item(0);
    if (file) {
      const text = await file.text();
      setFileData(text);
      setFileSelected(true);
    } else {
      setFileData("");
      setFileSelected(false);
    }
  };

  const handleReset = () => {
    setData(null);
    setFileData("");
    setFileSelected(false);
    const fileInput = document.getElementById("fileid") as HTMLInputElement;
    if (fileInput) {
      fileInput.value = "";
    }
  };

  return (
    <div className="w-full flex flex-row gap-5 h-screen">
      <div className="basis-1/3 ">
        <h2 className="text-xl my-4 font-normal border-b-[1.5px] border-zinc-200">
          Upload Java File Here
        </h2>
        <div className="">
          <input
            id="fileid"
            type="file"
            name="filename"
            onInput={handleFileInputEvent}
          ></input>
        </div>
      </div>
      <div className="basis-2/3">
        <h2 className="text-xl font-normal my-4 border-b-[1.5px] border-zinc-200">
          Unreachable Code
        </h2>
        <div className="flex justify-end pb-4">
          <button
            onClick={fetchData}
            disabled={!fileSelected}
            className={`mr-2 px-5 h-10 rounded-full text-white shadow-xl bg-black ${
              !fileSelected && "opacity-50 cursor-not-allowed"
            } hover:bg-zinc-700`}
          >
            View Unreachable Code
          </button>
          <button
            onClick={handleReset}
            disabled={!fileSelected}
            className={`bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded-full ${
              !fileSelected && "opacity-50 cursor-not-allowed"
            }`}
          >
            Reset
          </button>
        </div>
        <div className="bg-white h-[70vh] p-10 overflow-scroll">
          {fileSelected ? (
            <div>
              {data && (
                <div>
                  <ul>
                    {Object.entries(data).map(
                      ([functionName, statements], idx) => (
                        <li key={idx} className="mb-4">
                          <h3 className="text-lg font-semibold mb-2 border-b-[1px] border-gray-200">
                            {functionName}
                          </h3>
                          <ul>
                            {statements.map((statement, idx) => (
                              <li key={idx}>
                                <p className="mb-1">{statement}</p>
                              </li>
                            ))}
                          </ul>
                        </li>
                      )
                    )}
                  </ul>
                </div>
              )}
            </div>
          ) : (
            <p className="">Please select a file to view data.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default Page;
