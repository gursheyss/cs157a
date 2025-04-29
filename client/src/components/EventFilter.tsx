import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";

interface EventFilterProps {
  onCategoryChange: (category: string) => void;
  onSearchChange: (search: string) => void;
  selectedCategory: string;
  categories: string[];
}

const EventFilter = ({
  onCategoryChange,
  onSearchChange,
  selectedCategory,
  categories,
}: EventFilterProps) => {
  const [searchTerm, setSearchTerm] = useState("");

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    onSearchChange(searchTerm);
  };

  return (
    <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
      <div className="flex flex-col gap-4">
        <form onSubmit={handleSearch} className="relative">
          <Input
            placeholder="Search events..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pr-10"
          />
          <button
            type="submit"
            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500"
          >
            <Search className="h-4 w-4" />
          </button>
        </form>

        <div className="flex flex-wrap gap-2">
          {categories.map((category) => (
            <Button
              key={category}
              variant={selectedCategory === category ? "secondary" : "outline"}
              size="sm"
              className={`
                ${
                  selectedCategory === category
                    ? "bg-sjsu-gold text-black hover:bg-sjsu-gold/80"
                    : "hover:border-sjsu-gold hover:text-sjsu-blue"
                }
              `}
              onClick={() => onCategoryChange(category)}
            >
              {category}
            </Button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default EventFilter;
