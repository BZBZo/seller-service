$(document).ready(function() {
    $('body').on('click', '#button_red', function () {
        let productId = $('input[name="id"]').val();
        console.log(productId);

        if (!productId) {
            alert('상품 정보를 찾을 수 없습니다.');
            return;
        }

        if (confirm('이 상품을 정말 삭제하시겠습니까?')) {
            deleteProduct(productId);
        }
    });
});

// 상품 삭제 함수
function deleteProduct(productId) {
    $.ajax({
        type: 'DELETE',
        url: '/seller/product/detail_product/' + productId,  // 해당 상품 ID를 포함한 URL로 요청
        success: function(response) {
            if (response.success) {
                alert(response.message);  // 성공 메시지
                window.location.href = '/seller/product/list';  // 상품 목록 페이지로 리다이렉트
            } else {
                alert(response.message);  // 실패 메시지
            }
        },
        error: function(xhr, status, error) {
            console.error('오류 발생:', error);
            alert('상품 삭제 중 오류가 발생했습니다.');  // 오류 메시지
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const isCongValue = document.getElementById("isCong").value;

    // 값에 따라 상태를 설정
    const statusText = isCongValue === "true" ? "공구 가능" : "공구 불가";
    console.log(`현재 공구 진행 상태: ${statusText}`);

    // 상태를 화면에 표시
    const statusElement = document.querySelector('.product-status product-mt-2 span');
    if (statusElement) {
        statusElement.textContent = statusText;
    }
});