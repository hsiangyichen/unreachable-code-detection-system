import Link from "next/link";

const Navbar = () => {
  return (
    <nav className="w-full flex justify-between items-center py-4 h-[10vh]">
      <div className="flex justify-start items-center">
        <h1 className="text-black text-2xl font-bold">CodeDJ</h1>
      </div>
      <div className="flex justify-end items-center space-x-4">
        <NavLink href="/">Home</NavLink>
        <NavLink href="/upload">Upload File</NavLink>
        <NavLink href="/about">About</NavLink>
      </div>
    </nav>
  );
};

const NavLink = ({
  href,
  children,
}: {
  href: string;
  children: React.ReactNode;
}) => {
  return (
    <Link href={href}>
      <button className="text-zinc-500 hover:text-gray-400">{children}</button>
    </Link>
  );
};

export default Navbar;
