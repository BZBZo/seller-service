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
        url: '/seller/product/detail/' + productId,  // 해당 상품 ID를 포함한 URL로 요청
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

// 이 아래 39줄 ~ 70줄 주석들은 지워도 됌

// isCong 공구 가능/불가만 처리
// document.addEventListener("DOMContentLoaded", () => {
//     const isCongValue = document.getElementById("isCong").value;
//
//     // 값에 따라 상태를 설정
//     const statusText = isCongValue === "true" ? "공구 가능" : "공구 불가";
//     console.log(`현재 공구 진행 상태: ${statusText}`);
//
//     // 상태를 화면에 표시
//     const statusElement = document.querySelector('.product-status product-mt-2 span');
//     if (statusElement) {
//         statusElement.textContent = statusText;
//     }
// });

// isCong 공구 가능/불가와 condition 별도 이벤트로 처리
// document.addEventListener("DOMContentLoaded", () => {
//     const conditionInput = document.querySelector('input[name="condition"]').value;
//
//     if (conditionInput) {
//         // condition 값을 파싱
//         const conditionParts = conditionInput.replace(/[{}]/g, '').split(':');
//         const people = conditionParts[0] || 1;
//         const discount = conditionParts[1] || 0;
//
//         console.log(`모집 인원: ${people}, 할인율: ${discount}`);
//
//         // 화면에 표시
//         document.getElementById("peopleCount").textContent = `모집 인원: ${people}명`;
//         document.getElementById("discountRate").textContent = `할인율: ${discount}%`;
//     }
// });

// isCong & condition 동일 이벤트로 통합 처리
// DOMContentLoaded 이벤트: HTML 문서가 완전히 로드된 후 실행
document.addEventListener("DOMContentLoaded", () => {
    const isCongValue = document.getElementById("isCong").value; // 공구 진행 상태
    const conditionInput = document.querySelector('input[name="condition"]'); // condition 값
    const peopleCountElement = document.getElementById("peopleCount"); // 모집 인원 표시
    const discountRateElement = document.getElementById("discountRate"); // 할인율 표시

    console.log(`현재 공구 진행 상태: ${isCongValue}`); // 로그 출력

    if (isCongValue === "true") {
        if (conditionInput && conditionInput.value) {
            const conditionParts = conditionInput.value.replace(/[{}]/g, '').split(':');
            const people = conditionParts[0] || 1;
            const discount = conditionParts[1] || 0;

            // 모집 인원 및 할인율 설정
            if (peopleCountElement && discountRateElement) {
                peopleCountElement.textContent = `모집 인원: ${people}명`;
                discountRateElement.textContent = `할인율: ${discount}%`;
            }
        } else {
            console.warn("Condition 값이 비어 있습니다.");
        }
    } else {
        console.log("공구 진행 상태가 불가입니다. 모집 인원 및 할인율은 표시하지 않습니다.");
    }
});