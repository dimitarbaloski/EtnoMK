export default function Pagination({ currentPage, totalPages, onPageChange }) {
  if (!totalPages || totalPages < 1) return null;

  const pageWindow = 5;
  const halfWindow = Math.floor(pageWindow / 2);
  const startPage = Math.max(0, Math.min(currentPage - halfWindow, totalPages - pageWindow));
  const endPage = Math.min(totalPages, startPage + pageWindow);
  const pages = Array.from({ length: endPage - startPage }, (_, index) => startPage + index);

  return (
    <div className="pagination" aria-label="Pagination">
      <button
        type="button"
        className="btn btn-secondary"
        onClick={() => onPageChange(Math.max(currentPage - 1, 0))}
        disabled={currentPage === 0}
      >
        Previous
      </button>

      {startPage > 0 && (
        <>
          <button type="button" className="pagination-page" onClick={() => onPageChange(0)}>
            1
          </button>
          <span className="pagination-ellipsis">...</span>
        </>
      )}

      {pages.map((page) => (
        <button
          type="button"
          key={page}
          className={page === currentPage ? "pagination-page active" : "pagination-page"}
          onClick={() => onPageChange(page)}
          aria-current={page === currentPage ? "page" : undefined}
        >
          {page + 1}
        </button>
      ))}

      {endPage < totalPages && (
        <>
          <span className="pagination-ellipsis">...</span>
          <button type="button" className="pagination-page" onClick={() => onPageChange(totalPages - 1)}>
            {totalPages}
          </button>
        </>
      )}

      <button
        type="button"
        className="btn btn-secondary"
        onClick={() => onPageChange(Math.min(currentPage + 1, totalPages - 1))}
        disabled={currentPage >= totalPages - 1}
      >
        Next
      </button>
    </div>
  );
}
