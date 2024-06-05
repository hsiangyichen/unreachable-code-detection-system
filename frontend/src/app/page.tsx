import Link from "next/link";

export default function Home(): JSX.Element {
  return (
    <section className="h-[80vh] flex flex-col items-center justify-center">
      <h1 className="text-center text-7xl font-bold">Code Detective</h1>
      <p className="orange_gradient text-3xl text-center mt-2">
        Welcome to Unreachable Code Detection in Java Programs!
      </p>
      <p className="text-center text-lg w-[740px] my-8 text-zinc-500">
        Our mission is to provide developers with a powerful static analysis
        tool specifically designed to detect unreachable code in Java programs.
      </p>
      <Link href="/upload">
        <button className=" text-white font-bold h-10 px-4 transition-colors duration-300 ease-in-out rounded-full bg-black hover:bg-white hover:border hover:border-black hover:text-black">
          Start Your Analysis
        </button>
      </Link>
    </section>
  );
}
