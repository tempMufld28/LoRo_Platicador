document.addEventListener('DOMContentLoaded', () => {
    const downloadBtn = document.getElementById('downloadBtn');
    const toast = document.getElementById('toast');

    if (downloadBtn) {
        downloadBtn.addEventListener('click', () => {
            toast.classList.add('show');
            setTimeout(() => {
                toast.classList.remove('show');
            }, 4000);
        });
    }
});
