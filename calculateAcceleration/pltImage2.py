import matplotlib.pyplot as plt
import os


def calculateAccelerationPLT(data, save_dir):
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)
    array1 = data[0]
    array2 = data[1]

    # 绘制第一张图
    plt.figure()
    plt.plot(array1)
    plt.title('figure1')
    plt.xlabel('x')
    plt.ylabel('y')
    # 保存第一张图
    plt.savefig(os.path.join(save_dir, 'figure1.png'))
    plt.close()

    plt.figure()
    plt.plot(array2)
    plt.title('figure2')
    plt.xlabel('x')
    plt.ylabel('y')
    # 保存第一张图
    plt.savefig(os.path.join(save_dir, 'figure2.png'))
    plt.close()
